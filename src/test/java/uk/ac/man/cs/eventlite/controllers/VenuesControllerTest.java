package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.dao.DataIntegrityViolationException;

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
    
    // Add venue tests
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postValidVenue() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", "36 Bridge St")
            .param("postcode", "M3 3BT")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(handler().methodName("addVenue"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithoutName() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "")
            .param("address", "36 Bridge St")
            .param("postcode", "M3 3BT")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithoutAddress() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", "")
            .param("postcode", "M3 3BT")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "address"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithoutPostcode() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", "36 Bridge St")
            .param("postcode", "")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "postcode"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithNegativeCapacity() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", "36 Bridge St")
            .param("postcode", "M3 3BT")
            .param("capacity", "-5")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "capacity"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithZeroCapacity() throws Exception {
        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", "36 Bridge St")
            .param("postcode", "M3 3BT")
            .param("capacity", "0")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "capacity"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithLongName() throws Exception {
        String longName = "a".repeat(256);

        mvc.perform(post("/venues/add")
            .param("name", longName)
            .param("address", "36 Bridge St")
            .param("postcode", "M3 3BT")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void postVenueWithLongAddress() throws Exception {
        String longAddress = "a".repeat(300);

        mvc.perform(post("/venues/add")
            .param("name", "Manchester Hall")
            .param("address", longAddress)
            .param("postcode", "M1 1AA")
            .param("capacity", "200")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(handler().methodName("addVenue"))
            .andExpect(model().attributeHasFieldErrors("venue", "address"));
    }
    
    @Test
    public void getVenueById() throws Exception {
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));
        when(venue.getId()).thenReturn(1L);
        when(venue.getName()).thenReturn("Withworth Hall");
        when(venue.getAddress()).thenReturn("Oxford Road, Manchester");
        when(venue.getPostcode()).thenReturn("M13 9PL");
        when(venue.getCapacity()).thenReturn(500);

        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/show"))
            .andExpect(handler().methodName("getVenue"))
            .andExpect(model().attributeExists("venue"));

        verify(venueService).findById(1L);
        verifyNoMoreInteractions(venueService);
    }
    
    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteExistingVenueFlashesSuccessAndRedirects() throws Exception {
        when(venueService.findById(1L)).thenReturn(Optional.of(new Venue()));

        mvc.perform(delete("/venues/1").with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("successMessage", "Venue deleted."));

        verify(venueService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteMissingVenueFlashesErrorAndRedirects() throws Exception {
        when(venueService.findById(99999L)).thenReturn(Optional.empty());

        mvc.perform(delete("/venues/99999").with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("errorMessage", "Venue not found."));

        verify(venueService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteVenueWithEventsFlashesConstraintErrorAndRedirects() throws Exception {
        when(venueService.findById(1L)).thenReturn(Optional.of(new Venue()));
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(venueService).deleteById(1L);

        mvc.perform(delete("/venues/1").with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("errorMessage",
                    "Venue cannot be deleted because it has one or more events."));
    }
}
