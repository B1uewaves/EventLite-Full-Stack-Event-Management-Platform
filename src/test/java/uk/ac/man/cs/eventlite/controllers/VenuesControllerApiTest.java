package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import uk.ac.man.cs.eventlite.entities.Event;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class, EventModelAssembler.class })
public class VenuesControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private EventService eventService;

    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getAllVenues"))
            .andExpect(jsonPath("$.length()", equalTo(1)))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
            .andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile/venues")))
        	.andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile/venues")));

        verify(venueService).findAll();
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        Venue v = new Venue();
        v.setId(1);
        v.setName("Test Venue");
        v.setCapacity(100);

        when(venueService.findAll()).thenReturn(Collections.singletonList(v));

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getAllVenues"))
            .andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
            .andExpect(jsonPath("$._embedded.venues[0]._links.self.href", endsWith("/api/venues/1")))
            .andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("/api/venues/1/events")))
            .andExpect(jsonPath("$._embedded.venues[0]._links.next3events.href", endsWith("/api/venues/1/next3events")));

        verify(venueService).findAll();
    }

    @Test
    public void getVenueById() throws Exception {
        Venue v = new Venue();
        v.setId(1);
        v.setName("Test Venue");
        v.setCapacity(100);

        when(venueService.findById(1L)).thenReturn(Optional.of(v));

        mvc.perform(get("/api/venues/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(handler().methodName("getVenue"))
            .andExpect(jsonPath("$.name").value("Test Venue"))
            .andExpect(jsonPath("$.capacity").value(100))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1")))
            .andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/1")))
            .andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/1/events")))
            .andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/1/next3events")))
	        .andExpect(jsonPath("$.capacity").value(100))
	        .andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/1")));

        verify(venueService).findById(1L);
    }

    @Test
    public void getVenueNotFound() throws Exception {
        when(venueService.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(handler().methodName("getVenue"));
    }
    
    @Test
    public void getVenueEvents() throws Exception {
        Venue v = new Venue();
        v.setId(1);

        when(venueService.findById(1L)).thenReturn(Optional.of(v));

        mvc.perform(get("/api/venues/1/events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getVenueEvents"))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/events")));

        verify(venueService).findById(1L);
    }
    
    @Test
    public void getNext3Events() throws Exception {
        Venue v = new Venue();
        v.setId(1);

        when(venueService.findById(1L)).thenReturn(Optional.of(v));
        when(eventService.findNext3EventsByVenueId(anyLong(), any()))
            .thenReturn(Collections.emptyList());

        mvc.perform(get("/api/venues/1/next3events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getNext3Events"))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));

        verify(eventService).findNext3EventsByVenueId(anyLong(), any());
    }
    
    @Test
    public void getVenueEventsNotFound() throws Exception {
        when(venueService.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/venues/99/events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(handler().methodName("getVenueEvents"));

        verify(venueService).findById(99L);
    }
    
    @Test
    public void getNext3EventsNotFound() throws Exception {
        when(venueService.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/venues/99/next3events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(handler().methodName("getNext3Events"));

        verify(venueService).findById(99L);
    }
    
    @Test
    public void getVenueEventsWithEvents() throws Exception {
        Venue v = new Venue();
        v.setId(1);

        Event e = new Event();
        e.setId(10);
        e.setName("Test Event");
        e.setVenue(v);

        v.setEvents(Collections.singleton(e));

        when(venueService.findById(1L)).thenReturn(Optional.of(v));

        mvc.perform(get("/api/venues/1/events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getVenueEvents"))
            .andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
            .andExpect(jsonPath("$._embedded.events[0].name").value("Test Event"))
            .andExpect(jsonPath("$._embedded.events[0]._links.self.href", endsWith("/api/events/10")))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/events")));

        verify(venueService).findById(1L);
    }
    
    @Test
    public void getNext3EventsWithEvents() throws Exception {
        Venue v = new Venue();
        v.setId(1);

        Event e = new Event();
        e.setId(10);
        e.setName("Upcoming Event");
        e.setVenue(v);

        when(venueService.findById(1L)).thenReturn(Optional.of(v));
        when(eventService.findNext3EventsByVenueId(anyLong(), any()))
            .thenReturn(Collections.singletonList(e));

        mvc.perform(get("/api/venues/1/next3events")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getNext3Events"))
            .andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
            .andExpect(jsonPath("$._embedded.events[0].name").value("Upcoming Event"))
            .andExpect(jsonPath("$._embedded.events[0]._links.self.href", endsWith("/api/events/10")))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));

        verify(venueService).findById(1L);
        verify(eventService).findNext3EventsByVenueId(anyLong(), any());
    }


}
