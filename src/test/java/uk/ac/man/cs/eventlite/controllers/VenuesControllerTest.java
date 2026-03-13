package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

    @Autowired
    private MockMvc mvc;

    @Mock
    private Venue venue;

    @MockitoBean
    private VenueService venueService;

    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"))
                .andExpect(model().attributeExists("venues"));

        verify(venueService).findAll();
        verifyNoMoreInteractions(venueService);
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        when(venue.getName()).thenReturn("Kilburn Building");
        when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"))
                .andExpect(model().attributeExists("venues"));

        verify(venueService).findAll();
        verifyNoMoreInteractions(venueService);
    }

    @Test
    public void getIndexWithSearchTermUsesSearch() throws Exception {
        when(venueService.search("Kilburn")).thenReturn(Collections.<Venue>singletonList(venue));

        mvc.perform(get("/venues").param("search", "  Kilburn  ").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk()).andExpect(view().name("venues/index"))
                .andExpect(handler().methodName("getAllVenues"))
                .andExpect(model().attribute("searchTerm", "Kilburn"))
                .andExpect(model().attributeExists("venues"));

        verify(venueService).search("Kilburn");
        verifyNoMoreInteractions(venueService);
    }

    @Test
    public void getIndexWithBlankSearchFallsBackToFindAll() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

        mvc.perform(get("/venues").param("search", "   ").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"))
                .andExpect(model().attributeExists("venues"));

        verify(venueService).findAll();
        verifyNoMoreInteractions(venueService);
    }
    
    @Test
    public void getAddVenueForm() throws Exception {
        mvc.perform(get("/venues/add").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("showAddVenueForm"))
            .andExpect(model().attributeExists("venue"));
    }
}
