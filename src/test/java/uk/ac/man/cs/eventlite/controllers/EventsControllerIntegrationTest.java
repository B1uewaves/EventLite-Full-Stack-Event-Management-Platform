package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

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
public class EventsControllerIntegrationTest  {

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
    public void createEventSuccess() {
        client.post().uri("/events")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=My+Awesome+Concert&date=2026-10-31") 
                .exchange()
                .expectStatus().is3xxRedirection() // Expect a redirect after successful save
                .expectHeader().valueEquals("Location", "/events"); // Check it redirects to the right place
    }

	
		
}
