package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import uk.ac.man.cs.eventlite.entities.Event;

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
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest{
	@LocalServerPort
	private int port;

	private WebTestClient client;

	@Autowired
    private EventRepository eventRepository;


	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}

	@Test
    public void getSingleEventSuccess() {
        Event testEvent = new Event();
        testEvent.setName("Showcase 01");
        
        Event savedEvent = eventRepository.save(testEvent);

        client.get().uri("/events/" + savedEvent.getId()) 
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Showcase 01")); 
                });
    }


	@Test
    public void createEventRequiresAuth() {
        // Event creation lives at POST /my/events and is auth-protected;
        // an unauthenticated request must be redirected to sign-in.
        client.post().uri("/my/events")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=My+Awesome+Concert&date=2026-10-31")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", containsString("sign-in"));
    }

    @Test
    public void searchByNameReturnsMatchingEvent() {
        Event ev = new Event();
        ev.setName("Searchable Showcase");
        ev.setDate(LocalDate.now().plusDays(30));
        ev.setTime(LocalTime.of(10, 0));
        eventRepository.save(ev);

        client.get().uri(uri -> uri.path("/events").queryParam("search", "Searchable").build())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Searchable Showcase"));
                });
    }

    @Test
    public void searchWithNoMatchStillReturnsOk() {
        client.get().uri(uri -> uri.path("/events").queryParam("search", "ZZNothingHere").build())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    // The submitted term is echoed back into the search field via th:value="${searchTerm}".
                    assertThat(result.getResponseBody(), containsString("ZZNothingHere"));
                });
    }

    @Test
    public void eventsIndexContainsMapMarkup() {
        // Requirement 1: the events index must host a Mapbox map.
        client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body, containsString("id=\"map\""));
                    assertThat(body, containsString("mapbox-gl"));
                });
    }

    @Test
    public void singleEventPageContainsMapMarkup() {
        // Requirement 2: the event detail page must host a Mapbox map.
        Event ev = new Event();
        ev.setName("Map Markup Event");
        ev.setDate(LocalDate.now().plusDays(1));
        ev.setTime(LocalTime.of(12, 0));
        Event saved = eventRepository.save(ev);

        client.get().uri("/events/" + saved.getId()).accept(MediaType.TEXT_HTML).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body, containsString("id=\"map\""));
                    assertThat(body, containsString("mapbox-gl"));
                });
    }

    @Test
    public void updateEventBlockedWithoutAuthOrCsrf() {
        // POST /events/update/* requires the ADMIN role and a valid CSRF token.
        // An unauthenticated form post without a CSRF token must be rejected
        // (CsrfFilter returns 403 before the auth filters run).
        client.post().uri("/events/update/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=hax&date=2026-12-01")
                .exchange()
                .expectStatus().value(status -> assertTrue(
                        status == 302 || status == 401 || status == 403,
                        "expected redirect or 4xx for unauth/csrf-less POST, got " + status));
    }
}
