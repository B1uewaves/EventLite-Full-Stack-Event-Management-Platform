package uk.ac.man.cs.eventlite.controllers;

import java.util.Set;
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
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.Arrays;


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
import uk.ac.man.cs.eventlite.entities.Event;

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

// update venue tests - JJ 

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void getUpdateVenueForm() throws Exception {
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));

        mvc.perform(get("/venues/edit/1").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(handler().methodName("showUpdateForm"))
            .andExpect(model().attributeExists("venue"));

        verify(venueService).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateValidVenue() throws Exception {
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));


        mvc.perform(post("/venues/update/1")
            .param("name", "Updated Kilburn Building")
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(handler().methodName("updateVenue"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithoutName() throws Exception {
        mvc.perform(post("/venues/update/1")
            .param("name", "")
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "name"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithoutAddress() throws Exception {
        mvc.perform(post("/venues/update/1")
            .param("name", "Kilburn Building")
            .param("address", "")
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "address"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithNegativeCapacity() throws Exception {
        mvc.perform(post("/venues/update/1")
            .param("name", "Kilburn Building")
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "-10")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "capacity"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithZeroCapacity() throws Exception {
        mvc.perform(post("/venues/update/1")
            .param("name", "Kilburn Building")
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "0")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "capacity"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithLongName() throws Exception {
        String longName = "a".repeat(256);

        mvc.perform(post("/venues/update/1")
            .param("name", longName)
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "name"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void postUpdateVenueWithLongAddress() throws Exception {
        String longAddress = "a".repeat(300);

        mvc.perform(post("/venues/update/1")
            .param("name", "Kilburn Building")
            .param("address", longAddress)
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/update"))
            .andExpect(model().attributeHasFieldErrors("venue", "address"));
    }

    //  view all venues test - jj
    @Test
    public void getIndexDisplaysAllVenuesAlphabeticallyWithSummaryInfo() throws Exception {
        Venue venueA = new Venue();
        venueA.setId(1L);
        venueA.setName("Venue A");
        venueA.setAddress("23 Manchester Road");
        venueA.setPostcode("E14 3BD");
        venueA.setCapacity(50);

        Venue venueB = new Venue();
        venueB.setId(2L);
        venueB.setName("Venue B");
        venueB.setAddress("Highland Road");
        venueB.setPostcode("S43 2EZ");
        venueB.setCapacity(1000);

        Venue venueC = new Venue();
        venueC.setId(3L);
        venueC.setName("Venue C");
        venueC.setAddress("19 Acacia Avenue");
        venueC.setPostcode("WA15 8QY");
        venueC.setCapacity(10);

        
        when(venueService.findAll()).thenReturn(Arrays.asList(venueA, venueB, venueC));

        
        org.springframework.test.web.servlet.MvcResult result = mvc.perform(get("/venues").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/index"))
            .andExpect(handler().methodName("getAllVenues")) 
            .andReturn(); 

        // ignore the errors here since the model will never be null because we added venues to it in the mock
        // and the unchecked cast is safe because we know venues is a List
        List<Venue> modelVenues = (List<Venue>) result.getModelAndView().getModel().get("venues");


        assertNotNull(modelVenues);
        assertEquals(3, modelVenues.size());

        assertEquals("Venue A", modelVenues.get(0).getName());
        assertEquals("23 Manchester Road", modelVenues.get(0).getAddress());
        assertEquals(50, modelVenues.get(0).getCapacity());


        assertEquals("Venue B", modelVenues.get(1).getName());
        assertEquals("Highland Road", modelVenues.get(1).getAddress());
        assertEquals(1000, modelVenues.get(1).getCapacity());


        assertEquals("Venue C", modelVenues.get(2).getName());
        assertEquals("19 Acacia Avenue", modelVenues.get(2).getAddress());
        assertEquals(10, modelVenues.get(2).getCapacity());

        verify(venueService).findAll();
    }

    //  serach venues with whole word matches -jj
    @Test
    public void searchForVenueWithTermA() throws Exception {
        Venue venueA = new Venue();
        venueA.setId(1L);
        venueA.setName("Venue A");
        venueA.setAddress("23 Manchester Road");
        venueA.setCapacity(50);

        Venue venueB = new Venue();
        venueB.setId(2L);
        venueB.setName("Venue B");
        venueB.setAddress("Highland Road");
        venueB.setCapacity(1000);

        Venue venueC = new Venue();
        venueC.setId(3L);
        venueC.setName("Venues C");
        venueC.setAddress("19 Acacia Avenue");
        venueC.setCapacity(10);

        when(venueService.search("a")).thenReturn(Collections.singletonList(venueA));

        org.springframework.test.web.servlet.MvcResult result = mvc.perform(get("/venues")
            .param("search", "a")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/index"))
            .andExpect(model().attributeExists("venues", "searchTerm"))
            .andReturn();


        // ignore the errors here since the model will never be null because we added venues to it in the mock
        // and the unchecked cast is safe because we know venues is a List
        List<Venue> modelVenues = (List<Venue>) result.getModelAndView().getModel().get("venues");

        assertNotNull(modelVenues);
        assertEquals(1, modelVenues.size(), "Search for 'a' should return exactly 1 venue");
        
        assertEquals("Venue A", modelVenues.get(0).getName());
        assertEquals("23 Manchester Road", modelVenues.get(0).getAddress());

        verify(venueService).search("a");
    }


    @Test
    public void searchForVenueWithTermVenue() throws Exception {
        Event event = new Event();
        Set<Event> eventsC = new HashSet<>(); 
        eventsC.add(event);

        Venue venueA = new Venue();
        venueA.setId(1L);
        venueA.setName("Venue A");
        venueA.setDescription("cool event");
        venueA.setAddress("23 Manchester Road");
        venueA.setCapacity(50);

        Venue venueB = new Venue();
        venueB.setId(2L);
        venueB.setName("Venue B");
        venueB.setLongitude(85.0);
        venueB.setLatitude(85.0);
        venueB.setAddress("Highland Road");
        venueB.setCapacity(1000);

        Venue venueC = new Venue();
        venueC.setId(3L);
        venueC.setName("Venues C");
        venueC.setEvents(eventsC);
        venueC.setAddress("19 Acacia Avenue");
        venueC.setCapacity(10);

        when(venueService.search("venue")).thenReturn(Arrays.asList(venueA, venueB, venueC));

        org.springframework.test.web.servlet.MvcResult result = mvc.perform(get("/venues")
            .param("search", "venue")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/index"))
            .andExpect(model().attributeExists("venues", "searchTerm"))
            .andReturn();

        @SuppressWarnings({"unchecked", "null"})
        List<Venue> modelVenues = (List<Venue>) result.getModelAndView().getModel().get("venues");

        assertNotNull(modelVenues);
        assertEquals(3, modelVenues.size(), "Search for 'venue' should return 3 venues");
        
        assertEquals("Venue A", modelVenues.get(0).getName());
        assertEquals("Venue B", modelVenues.get(1).getName());

        verify(venueService).search("venue");
    }    

    @Test
    @WithMockUser("ADMINISTRATOR")
    public void venueWithSameName() throws Exception{
        when(venueService.existsByName("Venue A")).thenReturn(true);
        
        mvc.perform(post("/venues/add")
            .param("name", "Venue A")
            .param("address", "Oxford Road")
            .param("postcode", "M13 9PL")
            .param("capacity", "300")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/add"))
            .andExpect(model().attributeHasFieldErrors("venue", "name"));    
             
        verify(venueService).existsByName("Venue A");
        verify(venueService, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any(Venue.class));
    }


    @Test
    @WithMockUser("ADMINISTRATOR")
    public void getEditVenueFormWhenVenueNotFound() throws Exception {
        when(venueService.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/venues/99")
            .accept(MediaType.TEXT_HTML))
            
            .andExpect(status().isNotFound()) 
            .andExpect(view().name("venues/not_found")) 
            .andExpect(model().attributeExists("not_found_id")); 

        verify(venueService).findById(99L);
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
