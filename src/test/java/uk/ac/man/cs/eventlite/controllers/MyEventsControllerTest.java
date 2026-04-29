package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.when;

@WebMvcTest(MyEventsController.class)
@Import(Security.class)
public class MyEventsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private VenueService venueService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR", username = "Rob")
    public void postEventSuccess() throws Exception {
        mvc.perform(post("/my/events") // MyEventsController path
                .with(csrf())
                .param("name", "Valid Event")
                .param("date", "2026-12-31")
                .param("venue.id", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/my/events"));

        // call saveForUser, correct user name
        verify(eventService).saveForUser(any(Event.class), eq("Rob"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postEventWithMissingRequiredFields() throws Exception {
        mvc.perform(post("/my/events")
                .with(csrf())
                .param("name", "") // @NotBlank
                .param("date", "") // @NotNull
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("my/events/new"))
                .andExpect(model().attributeHasFieldErrors("event", "name"))
                .andExpect(model().attributeHasFieldErrors("event", "date"));
    }

	@Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteEventSuccess() throws Exception {
		Event event = new Event();
		event.setId(1L);
        when(eventService.findById(1L)).thenReturn(java.util.Optional.of(event));

        mvc.perform(delete("/my/events/1")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/my/events"))
                .andExpect(flash().attribute("successMessage", "Event deleted."));
        verify(eventService).deleteById(1L);
    }
}