package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Collections;

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
import uk.ac.man.cs.eventlite.services.MastodonService;

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
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());


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
		

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
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
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Share on Mastodon")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("placeholder=\"Java Meetup\"")));
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
	public void shareEventPreservesReturnToQueryParameterOnRedirect() throws Exception {
		Event event = new Event();
		event.setId(1);
		event.setName("Java Meetup");
		String postContent = "Join us at Java Meetup";
		String returnTo = "/events?search=java";

		when(eventService.findById(1)).thenReturn(java.util.Optional.of(event));

		mvc.perform(post("/events/1/share").param("postContent", postContent).param("returnTo", returnTo).with(csrf())
				.accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
				.andExpect(redirectedUrl("/events/1?returnTo=%2Fevents%3Fsearch%3Djava"))
				.andExpect(handler().methodName("shareEvent"));
	}
}
