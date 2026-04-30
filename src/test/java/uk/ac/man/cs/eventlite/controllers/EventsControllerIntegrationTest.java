package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventRepository;
import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.services.MastodonService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private VenueRepository venueRepository;

	@MockitoBean
	private MastodonService mastodonService;

	@BeforeEach
	public void setup() {
		reset(mastodonService);
		when(mastodonService.getLatestPosts()).thenReturn(Collections.emptyList());
	}

	@Test
	public void getAllEventsPassesEventsVenuesAndPostsToModel() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration List Event", LocalDate.now().plusDays(30)));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(model().attributeExists("events", "upcomingEvents", "pastEvents", "venues", "posts"))
				.andExpect(model().attribute("upcomingEvents",
						hasItem(hasProperty("name", equalTo("Integration List Event")))))
				.andExpect(model().attribute("venues", hasItem(hasProperty("id", equalTo(savedEvent.getVenue().getId())))))
				.andExpect(content().string(containsString("All Events")))
				.andExpect(content().string(containsString("id=\"map\"")))
				.andExpect(content().string(containsString("mapbox-gl")));
	}

	@Test
	public void searchEventsPassesMatchingEventsAndSearchTermToModel() throws Exception {
		Venue venue = existingVenue();
		eventRepository.save(event("IntegrationSearch Alpha", LocalDate.now().plusDays(2), venue));
		eventRepository.save(event("IntegrationSearch Beta", LocalDate.now().plusDays(2), venue));
		eventRepository.save(event("IntegrationSearch Past", LocalDate.now().minusDays(1), venue));
		eventRepository.save(event("IntegrationSearchable Partial", LocalDate.now().plusDays(3), venue));

		mvc.perform(get("/events").param("search", "IntegrationSearch").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(model().attribute("searchTerm", "IntegrationSearch"))
				.andExpect(model().attribute("upcomingEvents", contains(
						hasProperty("name", equalTo("IntegrationSearch Alpha")),
						hasProperty("name", equalTo("IntegrationSearch Beta")))))
				.andExpect(model().attribute("pastEvents",
						contains(hasProperty("name", equalTo("IntegrationSearch Past")))))
				.andExpect(content().string(containsString("IntegrationSearch Alpha")))
				.andExpect(content().string(not(containsString("IntegrationSearchable Partial"))));
	}

	@Test
	public void searchEventsWithNoMatchPassesEmptyResultsAndSearchTermToModel() throws Exception {
		mvc.perform(get("/events").param("search", "ZZNothingHere").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(model().attribute("searchTerm", "ZZNothingHere"))
				.andExpect(model().attribute("upcomingEvents", empty()))
				.andExpect(model().attribute("pastEvents", empty()))
				.andExpect(content().string(containsString("ZZNothingHere")));
	}

	@Test
	public void getSingleEventPassesEventAndDefaultReturnToToModel() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Detail Event", LocalDate.now().plusDays(4)));

		mvc.perform(get("/events/" + savedEvent.getId()).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/show"))
				.andExpect(model().attribute("event", hasProperty("name", equalTo("Integration Detail Event"))))
				.andExpect(model().attribute("event", hasProperty("date", equalTo(savedEvent.getDate()))))
				.andExpect(model().attribute("event", hasProperty("time", equalTo(savedEvent.getTime()))))
				.andExpect(model().attribute("event",
						hasProperty("venue", hasProperty("name", equalTo(savedEvent.getVenue().getName())))))
				.andExpect(model().attribute("returnTo", "/events"))
				.andExpect(content().string(containsString("Integration Detail Event")))
				.andExpect(content().string(containsString("id=\"map\"")))
				.andExpect(content().string(containsString("mapbox-gl")));
	}

	@Test
	public void getSingleEventPreservesReturnToParameter() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Return Event", LocalDate.now().plusDays(5)));

		mvc.perform(get("/events/" + savedEvent.getId())
				.param("returnTo", "/venues")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/show"))
				.andExpect(model().attribute("event", hasProperty("name", equalTo("Integration Return Event"))))
				.andExpect(model().attribute("returnTo", "/venues"));
	}

	@Test
	public void getEventNotFoundPassesMissingIdToModel() throws Exception {
		mvc.perform(get("/events/99999").accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"))
				.andExpect(model().attribute("not_found_id", 99999L))
				.andExpect(content().string(containsString("99999")));
	}

	@Test
	public void getEditEventPassesEventVenuesAndRedirectToModel() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Edit Event", LocalDate.now().plusDays(6)));

		mvc.perform(get("/events/edit/" + savedEvent.getId())
				.param("redirect", "/my/events")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(model().attribute("event", hasProperty("name", equalTo("Integration Edit Event"))))
				.andExpect(model().attributeExists("venues"))
				.andExpect(model().attribute("redirect", "/my/events"));
	}

	@Test
	public void getEditEventNotFoundPassesMissingIdToModel() throws Exception {
		mvc.perform(get("/events/edit/99999").accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"))
				.andExpect(model().attribute("not_found_id", 99999L));
	}

	@Test
	public void getMyEventsPassesEventsToModel() throws Exception {
		mvc.perform(get("/my/events")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("my/events/index"))
				.andExpect(model().attributeExists("events"));
	}

	@Test
	public void getNewEventPassesEventAndVenuesToModel() throws Exception {
		mvc.perform(get("/my/events/new")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("my/events/new"))
				.andExpect(model().attributeExists("event", "venues"));
	}

	@Test
	public void postUpdateEventWithSensibleDataUpdatesEventAndRedirects() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Update Event", LocalDate.now().plusDays(7)));
		Venue venue = existingVenue();

		mvc.perform(post("/events/update/" + savedEvent.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Updated Integration Event")
				.param("date", LocalDate.now().plusDays(8).toString())
				.param("time", "14:30")
				.param("venue.id", venue.getId().toString())
				.param("description", "Updated description")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events"))
				.andExpect(redirectedUrl("/events"));

		Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
		assertEquals("Updated Integration Event", updatedEvent.getName());
		assertEquals(LocalDate.now().plusDays(8), updatedEvent.getDate());
		assertEquals(LocalTime.of(14, 30), updatedEvent.getTime());
		assertEquals(venue.getId(), updatedEvent.getVenue().getId());
		assertEquals("Updated description", updatedEvent.getDescription());
	}

	@Test
	public void postUpdateEventWithMissingDataReturnsFormWithModelErrors() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Missing Update Event", LocalDate.now().plusDays(9)));

		mvc.perform(post("/events/update/" + savedEvent.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "")
				.param("date", "")
				.param("time", "12:00")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(model().attributeExists("event", "venues"))
				.andExpect(model().attributeHasFieldErrors("event", "name", "date", "venue"));
	}

	@Test
	public void postUpdateEventWithInvalidDataReturnsFormWithModelErrors() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Invalid Update Event", LocalDate.now().plusDays(10)));
		Venue venue = existingVenue();

		mvc.perform(post("/events/update/" + savedEvent.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Invalid Update Event")
				.param("date", LocalDate.now().minusDays(1).toString())
				.param("time", "12:00")
				.param("venue.id", venue.getId().toString())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(model().attributeExists("event", "venues"))
				.andExpect(model().attributeHasFieldErrors("event", "date"));
	}

	@Test
	public void postUpdateEventAsUnauthorisedUserRedirectsToSignIn() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Unauthorised Update Event", LocalDate.now().plusDays(11)));
		Venue venue = existingVenue();

		mvc.perform(post("/events/update/" + savedEvent.getId())
				.with(csrf())
				.param("name", "Blocked Update")
				.param("date", LocalDate.now().plusDays(12).toString())
				.param("time", "12:00")
				.param("venue.id", venue.getId().toString())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("**/sign-in"));
	}

	@Test
	public void postShareEventWithSensibleDataPublishesStatusAndRedirects() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Share Event", LocalDate.now().plusDays(13)));

		mvc.perform(post("/events/" + savedEvent.getId() + "/share")
				.with(csrf())
				.param("postContent", "Join this integration event")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/" + savedEvent.getId()))
				.andExpect(flash().attribute("successMessage",
						"Mastodon status published: Join this integration event"));

		verify(mastodonService).publishStatus("Join this integration event");
	}

	@Test
	public void postShareEventWithMissingDataRedirectsWithModelFlashError() throws Exception {
		Event savedEvent = eventRepository.save(event("Integration Missing Share Event", LocalDate.now().plusDays(14)));

		mvc.perform(post("/events/" + savedEvent.getId() + "/share")
				.with(csrf())
				.param("postContent", "   ")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/" + savedEvent.getId()))
				.andExpect(flash().attribute("errorMessage", "Please enter some content before sharing."));

		verify(mastodonService, never()).publishStatus(anyString());
	}

	@Test
	public void postShareEventWithInvalidEventIdReturnsNotFound() throws Exception {
		mvc.perform(post("/events/99999/share")
				.with(csrf())
				.param("postContent", "This event does not exist")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"))
				.andExpect(model().attribute("not_found_id", 99999L));

		verify(mastodonService, never()).publishStatus(anyString());
	}

	@Test
	public void postCreateEventWithSensibleDataSavesEventAndRedirects() throws Exception {
		Venue venue = existingVenue();

		mvc.perform(post("/my/events")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Create Event")
				.param("date", LocalDate.now().plusDays(15).toString())
				.param("time", "15:45")
				.param("venue.id", venue.getId().toString())
				.param("description", "Created from integration test")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/my/events"))
				.andExpect(redirectedUrl("/my/events"));

		Optional<Event> savedEvent = eventByName("Integration Create Event");
		assertTrue(savedEvent.isPresent());
		assertEquals("Alice", savedEvent.get().getOwnerUsername());
	}

	@Test
	public void postCreateEventWithMissingDataReturnsFormWithModelErrors() throws Exception {
		mvc.perform(post("/my/events")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "")
				.param("date", "")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("my/events/new"))
				.andExpect(model().attributeExists("event", "venues"))
				.andExpect(model().attributeHasFieldErrors("event", "name", "date", "venue"));
	}

	@Test
	public void postCreateEventWithInvalidDataReturnsFormWithModelErrors() throws Exception {
		Venue venue = existingVenue();

		mvc.perform(post("/my/events")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Invalid Create Event")
				.param("date", LocalDate.now().minusDays(1).toString())
				.param("time", "09:30")
				.param("venue.id", venue.getId().toString())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("my/events/new"))
				.andExpect(model().attributeExists("event", "venues"))
				.andExpect(model().attributeHasFieldErrors("event", "date"));
	}

	@Test
	public void postCreateEventAsUnauthorisedUserRedirectsToSignIn() throws Exception {
		Venue venue = existingVenue();

		mvc.perform(post("/my/events")
				.with(csrf())
				.param("name", "Unauthorised Create Event")
				.param("date", LocalDate.now().plusDays(16).toString())
				.param("time", "10:00")
				.param("venue.id", venue.getId().toString())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("**/sign-in"));
	}

	private Event event(String name, LocalDate date) {
		return event(name, date, existingVenue());
	}

	private Event event(String name, LocalDate date, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDate(date);
		event.setTime(LocalTime.NOON);
		event.setVenue(venue);
		event.setDescription("Integration test event");
		return event;
	}

	private Venue existingVenue() {
		return venueRepository.findAll().iterator().next();
	}

	private Optional<Event> eventByName(String name) {
		return StreamSupport.stream(eventRepository.findAll().spliterator(), false)
				.filter(event -> name.equals(event.getName()))
				.findFirst();
	}
}
