package uk.ac.man.cs.eventlite.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
@Profile("!test")
public class PostcodeGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(PostcodeGeocodingService.class);

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, Coordinates> cache = new ConcurrentHashMap<>();

    public PostcodeGeocodingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    /**
     * Best-effort populate {@link Venue#getLatitude()} and
     * {@link Venue#getLongitude()} from
     * the venue's postcode (UK) using https://postcodes.io.
     *
     * If the venue is missing a postcode, already has coordinates, or the lookup
     * fails,
     * the venue is left unchanged.
     */
    public boolean populateCoordinatesFromPostcodeIfMissing(Venue venue) {
        if (venue == null) {
            return false;
        }

        boolean latitudeMissing = venue.getLatitude() == null;
        boolean longitudeMissing = venue.getLongitude() == null;
        if (!latitudeMissing && !longitudeMissing) {
            return false;
        }

        String postcode = normalisePostcode(venue.getPostcode());
        if (!StringUtils.hasText(postcode)) {
            return false;
        }

        Coordinates cached = cache.get(postcode);
        if (cached != null) {
            if (latitudeMissing) {
                venue.setLatitude(cached.latitude());
            }
            if (longitudeMissing) {
                venue.setLongitude(cached.longitude());
            }
            return true;
        }

        Coordinates lookedUp = lookupCoordinates(postcode);
        if (lookedUp == null) {
            return false;
        }

        cache.put(postcode, lookedUp);
        if (latitudeMissing) {
            venue.setLatitude(lookedUp.latitude());
        }
        if (longitudeMissing) {
            venue.setLongitude(lookedUp.longitude());
        }
        return true;
    }

    private Coordinates lookupCoordinates(String postcode) {
        try {
            String encoded = URLEncoder.encode(postcode, StandardCharsets.UTF_8);
            URI uri = URI.create("https://api.postcodes.io/postcodes/" + encoded);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.debug("Postcode lookup failed for {} (HTTP {})", postcode, response.statusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root == null ? null : root.get("result");
            if (result == null || result.isNull()) {
                return null;
            }

            JsonNode latNode = result.get("latitude");
            JsonNode lonNode = result.get("longitude");
            if (latNode == null || lonNode == null || latNode.isNull() || lonNode.isNull()) {
                return null;
            }

            return new Coordinates(latNode.asDouble(), lonNode.asDouble());
        } catch (IOException ex) {
            log.debug("Postcode lookup IO failure for {}", postcode, ex);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.debug("Postcode lookup interrupted for {}", postcode, ex);
            return null;
        } catch (RuntimeException ex) {
            log.debug("Postcode lookup parsing failure for {}", postcode, ex);
            return null;
        }
    }

    private static String normalisePostcode(String postcode) {
        if (!StringUtils.hasText(postcode)) {
            return "";
        }

        // Collapse whitespace and uppercase (UK postcodes are case-insensitive).
        return postcode.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private record Coordinates(double latitude, double longitude) {
    }
}
