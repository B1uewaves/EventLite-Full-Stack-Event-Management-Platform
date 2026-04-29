package uk.ac.man.cs.eventlite.services;

import java.util.ArrayList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Timelines;

// switched from GSON/Mastodon4j to Jackson (Standard Spring JSON tool)
// jackson is more robust and already built into Spring Boot
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import okhttp3.OkHttpClient;

@Service
public class MastodonServiceImpl implements MastodonService {

	private final RestClient restClient;

	@Value("${mastodon.instance-url:}")
	private String instanceUrl;

	@Value("${mastodon.access-token:}")
	private String accessToken;

	@Value("${mastodon.visibility:unlisted}")
	private String visibility;

	public MastodonServiceImpl() {
		// using RestClient for publishing and fetching
        // it is better than the OkHttpClient inside the mastodon4j library, which was causing  timeouts 
		restClient = RestClient.create();
	}

	@Override
	public void publishStatus(String content) {
		if (!StringUtils.hasText(instanceUrl) || !StringUtils.hasText(accessToken)) {
			throw new IllegalStateException("Mastodon is not configured.");
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("status", content);
		form.add("visibility", visibility);

		restClient.post()
				.uri(normalisedInstanceUrl() + "/api/v1/statuses")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();
	}

    @Override
    public List<MastodonPost> getLatestPosts() {
        try {

			
            String rawJson = restClient.get()
                    .uri(normalisedInstanceUrl() + "/api/v1/timelines/home?limit=40")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

			
			// converts rawJson and converts into a list of maps 
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> posts = mapper.readValue(rawJson, new TypeReference<List<Map<String, Object>>>() {});

            return posts.stream()
                    .filter(p -> {
                        Map<String, Object> account = (Map<String, Object>) p.get("account");
                        return account != null && "manc_bee_f12".equals(account.get("username")); // look through manuall since we dont have .getUsername()
                    })

                    .map(p -> { //converts into mastadon object
                        String createdAt = (String) p.get("created_at");
                        ZonedDateTime ukTime = Instant.parse(createdAt).atZone(ZoneId.of("Europe/London"));

                        MastodonPost mp = new MastodonPost();
                        mp.setTime(ukTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        mp.setDate(ukTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        mp.setContent((String) p.get("content"));
                        mp.setUrl((String) p.get("url"));
                        mp.setRawDate(ukTime); 

                        return mp;
                    })
                    .sorted((a, b) -> b.getRawDate().compareTo(a.getRawDate())) // Sort chronologically
                    .limit(3)
                    .toList();

        } catch (Exception e) {
            System.err.println("Failed to fetch Mastodon posts: " + e.getMessage());
            return List.of();
        }
    }


	private String normalisedInstanceUrl() {
		if (instanceUrl.endsWith("/")) {
			return instanceUrl.substring(0, instanceUrl.length() - 1);
		}

		return instanceUrl;
	}
}