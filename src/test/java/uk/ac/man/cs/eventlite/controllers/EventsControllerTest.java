package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.services.MastodonPost;
import uk.ac.man.cs.eventlite.services.MastodonService;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockitoBean
	private EventService eventService;

	@MockitoBean
	private VenueService venueService;

	@MockitoBean
	private MastodonService mastodonService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(Collections.<Event>emptyList());
		when(eventService.findPastEvents(any(LocalDate.class))).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		when(mastodonService.getLatestPosts()).thenReturn(Collections.emptyList());


		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));
		when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(Collections.<Event>singletonList(event));
		when(eventService.findPastEvents(any(LocalDate.class))).thenReturn(Collections.<Event>emptyList());
		when(mastodonService.getLatestPosts()).thenReturn(Collections.emptyList());
		

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithSearchSplitsAndSortsEvents() throws Exception {
		LocalDate today = LocalDate.now();
		Event betaUpcoming = event("Beta Workshop", today.plusDays(2), venue("Kilburn Building"));
		Event alphaUpcoming = event("Alpha Workshop", today.plusDays(2), venue("Simon Building"));
		Event recentPast = event("Recent Past", today.minusDays(1), venue("Renold Building"));
		Event olderPast = event("Older Past", today.minusDays(3), venue("University Place"));

		when(eventService.search("workshop")).thenReturn(Arrays.asList(olderPast, betaUpcoming, recentPast, alphaUpcoming));
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		when(mastodonService.getLatestPosts()).thenReturn(Collections.emptyList());

		mvc.perform(get("/events").param("search", " workshop ").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index"))
				.andExpect(handler().methodName("getAllEvents"))
				.andExpect(model().attribute("upcomingEvents", contains(alphaUpcoming, betaUpcoming)))
				.andExpect(model().attribute("pastEvents", contains(recentPast, olderPast)))
				.andExpect(model().attribute("searchTerm", " workshop "));

		verify(eventService).search("workshop");
		verify(venueService).findAll();
	}

	@Test
	public void getIndexShowsMastodonPostDateAndTimeSeparately() throws Exception {
		MastodonPost post = new MastodonPost();
		post.setTime("14:19");
		post.setDate("2026-04-24");
		post.setContent("A useful Mastodon post");
		post.setUrl("https://defcon.social/@manc_bee_f12/1");

		when(mastodonService.getLatestPosts()).thenReturn(Collections.singletonList(post));
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("https://defcon.social/@manc_bee_f12/1")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("2026-04-24")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("14:19")));
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}

	@Test
	public void getEventShowsMastodonShareFormWithEventNamePlaceholder() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");

		when(eventService.findById(1)).thenReturn(java.util.Optional.of(event));

		mvc.perform(get("/events/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/show")).andExpect(handler().methodName("getEvent"))
				.andExpect(model().attribute("event", event))
				.andExpect(model().attribute("returnTo", "/events"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Share on Mastodon")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("placeholder=\"Java Meetup\"")));
	}

	@Test
	public void getEventPreservesReturnToParameter() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");

		when(eventService.findById(1)).thenReturn(Optional.of(event));

		mvc.perform(get("/events/1").param("returnTo", "/venues").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/show"))
				.andExpect(handler().methodName("getEvent"))
				.andExpect(model().attribute("event", event))
				.andExpect(model().attribute("returnTo", "/venues"));
	}

	@Test
	public void shareEventPublishesMastodonStatusAndConfirmsOriginalPost() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");
		String postContent = "Join us at Java Meetup";

		when(eventService.findById(1)).thenReturn(java.util.Optional.of(event));

		mvc.perform(post("/events/1/share").param("postContent", postContent).with(csrf()).accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound()).andExpect(redirectedUrl("/events/1"))
				.andExpect(flash().attribute("successMessage", "Mastodon status published: " + postContent))
				.andExpect(handler().methodName("shareEvent"));

		verify(mastodonService).publishStatus(postContent);
	}

	@Test
	public void shareEventRejectsBlankStatusWithoutPublishing() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");

		when(eventService.findById(1)).thenReturn(Optional.of(event));

		mvc.perform(post("/events/1/share").param("postContent", "   ").with(csrf()).accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/1"))
				.andExpect(flash().attribute("errorMessage", "Please enter some content before sharing."))
				.andExpect(handler().methodName("shareEvent"));

		verify(mastodonService, never()).publishStatus(any());
	}

	@Test
	public void shareEventReportsPublishFailure() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");

		when(eventService.findById(1)).thenReturn(Optional.of(event));
		doThrow(new RuntimeException("Mastodon unavailable")).when(mastodonService).publishStatus("Join us");

		mvc.perform(post("/events/1/share").param("postContent", "Join us").with(csrf()).accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/1"))
				.andExpect(flash().attribute("errorMessage", "The Mastodon status could not be published."))
				.andExpect(handler().methodName("shareEvent"));

		verify(mastodonService).publishStatus("Join us");
	}

	@Test
	public void shareEventPreservesReturnToQueryParameterOnRedirect() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");
		String postContent = "Join us at Java Meetup";
		String returnTo = "/events?search=java";

		when(eventService.findById(1)).thenReturn(java.util.Optional.of(event));

		mvc.perform(post("/events/1/share").param("postContent", postContent).param("returnTo", returnTo).with(csrf())
				.accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/1?returnTo=%2Fevents%3Fsearch=java"))
				.andExpect(handler().methodName("shareEvent"));
	}

	@Test
	public void getEditEventShowsUpdateForm() throws Exception {
		Event event = event("Java Meetup", LocalDate.now().plusDays(2), venue("Kilburn Building"));
		event.setId(1);
		Venue venue = venue("Kilburn Building");

		when(eventService.findById(1L)).thenReturn(Optional.of(event));
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

		mvc.perform(get("/events/edit/1").param("redirect", "/my/events").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(handler().methodName("showUpdateForm"))
				.andExpect(model().attribute("event", event))
				.andExpect(model().attribute("venues", contains(venue)))
				.andExpect(model().attribute("redirect", "/my/events"));
	}

	@Test
	public void getEditEventNotFound() throws Exception {
		when(eventService.findById(99L)).thenReturn(Optional.empty());

		mvc.perform(get("/events/edit/99").accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"))
				.andExpect(handler().methodName("showUpdateForm"))
				.andExpect(model().attribute("not_found_id", 99L));
	}

	@Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateEventSuccess() throws Exception {
		// update mock obj
		Event existingEvent = new Event();
		existingEvent.setId(1L);
		Venue v = new Venue();
		v.setId(1L);

        when(eventService.findById(1L)).thenReturn(java.util.Optional.of(existingEvent));
        when(venueService.findAll()).thenReturn(Collections.singletonList(v));

        mvc.perform(post("/events/update/1")
                .with(csrf())
                .param("name", "Updated Name")
                .param("date", "2026-12-31")
                .param("time", "12:00")
                .param("venue.id", "1")
                .param("description", "New Description")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        // call save
        verify(eventService).save(any(Event.class));
    }

	@Test
	@WithMockUser(roles = "ADMINISTRATOR")
	public void postUpdateEventUsesRedirectParameter() throws Exception {
		Event existingEvent = new Event();
		existingEvent.setId(1L);
		Venue venue = venue("Kilburn Building");
		venue.setId(1L);

		when(eventService.findById(1L)).thenReturn(Optional.of(existingEvent));

		mvc.perform(post("/events/update/1")
				.with(csrf())
				.param("redirect", "/my/events")
				.param("name", "Updated Name")
				.param("date", LocalDate.now().plusDays(7).toString())
				.param("time", "12:00")
				.param("venue.id", "1")
				.param("description", "New Description")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/my/events"));

		verify(eventService).save(any(Event.class));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATOR")
	public void postUpdateEventWithValidationErrorsReturnsUpdateForm() throws Exception {
		Venue venue = venue("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

		mvc.perform(post("/events/update/1")
				.with(csrf())
				.param("redirect", "/my/events")
				.param("name", "")
				.param("date", "")
				.param("time", "12:00")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(handler().methodName("updateEvent"))
				.andExpect(model().attribute("venues", contains(venue)))
				.andExpect(model().attribute("redirect", "/my/events"))
				.andExpect(model().attributeHasFieldErrors("event", "name", "date", "venue"));

		verify(eventService, never()).save(any(Event.class));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATOR")
	public void postUpdateEventNotFound() throws Exception {
		when(eventService.findById(99L)).thenReturn(Optional.empty());

		mvc.perform(post("/events/update/99")
				.with(csrf())
				.param("name", "Updated Name")
				.param("date", LocalDate.now().plusDays(7).toString())
				.param("time", "12:00")
				.param("venue.id", "1")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"))
				.andExpect(handler().methodName("updateEvent"))
				.andExpect(model().attribute("not_found_id", 99L));
	}

	private static Event event(String name, LocalDate date, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDate(date);
		event.setTime(LocalTime.NOON);
		event.setVenue(venue);
		return event;
	}

	private static Venue venue(String name) {
		Venue venue = new Venue();
		venue.setName(name);
		venue.setCapacity(100);
		return venue;
	}
/*
	@Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postEventWithMissingRequiredFields() throws Exception {
        mvc.perform(post("/events/add")
                .with(csrf())
                .param("name", "")        // @NotBlank
                .param("date", "")        // @NotNull
                .param("venue.id", "")    // @NotNullss
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("events/add"))
                .andExpect(model().attributeHasFieldErrors("event", "name"))
                .andExpect(model().attributeHasFieldErrors("event", "date"))
                .andExpect(model().attributeHasFieldErrors("event", "venue"));
    }

	@Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postEventSuccess() throws Exception {
        mvc.perform(post("/events/add")
                .with(csrf())
                .param("name", "New Year Party")
                .param("date", "2026-12-31")
                .param("time", "23:59")
                .param("venue.id", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).save(org.mockito.ArgumentMatchers.any(Event.class));
    }
		*/
}
