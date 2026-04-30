package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@Autowired
	private VenueRepository venueRepository;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				.responseTimeout(Duration.ofSeconds(30))
				.build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					String body = result.getResponseBody();
					assertThat(body, containsString("All Venues"));
					assertThat(body, containsString("Alan Turing"));
					assertThat(body, containsString("Crawford House"));
					assertThat(body, containsString("Withworth Hall"));
				});
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99999").accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					String body = result.getResponseBody();
					assertThat(body, containsString("Venue Not Found"));
					assertThat(body, containsString("99999"));
				});
	}

	@Test
	public void getSingleVenueSuccess() {
		Venue savedVenue = venueRepository.save(venue("Integration Test Venue", 321));

		client.get().uri("/venues/" + savedVenue.getId()).accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					String body = result.getResponseBody();
					assertThat(body, containsString("Integration Test Venue"));
					assertThat(body, containsString("Oxford Road, Manchester"));
					assertThat(body, containsString("M13 9PL"));
					assertThat(body, containsString("321"));
				});
	}

	@Test
	public void searchByNameReturnsMatchingVenue() {
		client.get().uri(uri -> uri.path("/venues").queryParam("search", "Alan").build())
				.accept(MediaType.TEXT_HTML)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					String body = result.getResponseBody();
					assertThat(body, containsString("Alan Turing"));
					assertThat(body, not(containsString("Crawford House")));
				});
	}

	@Test
	public void searchWithNoMatchStillReturnsOk() {
		client.get().uri(uri -> uri.path("/venues").queryParam("search", "ZZNothingHere").build())
				.accept(MediaType.TEXT_HTML)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					String body = result.getResponseBody();
					assertThat(body, containsString("No venues found matching"));
					assertThat(body, containsString("ZZNothingHere"));
				});
	}

	@Test
	public void updateVenueBlockedWithoutAuthOrCsrf() {
		client.post().uri("/venues/update/1")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue("name=hax&address=Oxford+Road,+Manchester&postcode=M13+9PL&capacity=200")
				.exchange()
				.expectStatus().value(status -> assertTrue(
						status == 302 || status == 401 || status == 403,
						"expected redirect or 4xx for unauth/csrf-less POST, got " + status));
	}

	private Venue venue(String name, int capacity) {
		Venue venue = new Venue();
		venue.setName(name);
		venue.setAddress("Oxford Road, Manchester");
		venue.setPostcode("M13 9PL");
		venue.setCapacity(capacity);
		return venue;
	}
}
