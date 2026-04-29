package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeleteVenueAcceptanceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VenueRepository venueRepository;

    private Venue getVenueByName(String name) {
        Iterable<Venue> venues = venueRepository.findAll();
        Iterator<Venue> it = venues.iterator();

        while (it.hasNext()) {
            Venue v = it.next();
            if (name.equals(v.getName())) {
                return v;
            }
        }

        throw new IllegalStateException(name + " not found in test database");
    }

    @Test
    public void deleteIsNotAvailableFromVenuesListPage() throws Exception {
        mvc.perform(get("/venues"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("action=\"/venues/"))));
    }

    @Test
    public void deleteIsAvailableFromVenueDetailPageWhenLoggedIn() throws Exception {
        Venue venue = getVenueByName("Display Delete Venue");

        mvc.perform(get("/venues/" + venue.getId())
                .with(user("Alice").roles("ADMINISTRATOR")))
        	.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("action=\"/venues/" + venue.getId())));
    }

    @Test
    public void deletingVenueRemovesIt() throws Exception {
        Venue venue = getVenueByName("Delete Test Venue");
        long id = venue.getId();

        mvc.perform(delete("/venues/" + id)
                .with(user("Alice").roles("ADMINISTRATOR"))
                .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("successMessage", "Venue deleted."));

        assertFalse(venueRepository.findById(id).isPresent());
    }

    @Test
    public void deletingVenueWithEventsFails() throws Exception {
        Venue venue = getVenueByName("Test Venue");
        long id = venue.getId();

        mvc.perform(delete("/venues/" + id)
                .with(user("Alice").roles("ADMINISTRATOR"))
                .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("errorMessage",
                    "Venue cannot be deleted because it has one or more events."));

        assertTrue(venueRepository.findById(id).isPresent());
    }

    @Test
    public void deletingMissingVenueRedirectsWithError() throws Exception {
        mvc.perform(delete("/venues/99999")
                .with(user("Alice").roles("ADMINISTRATOR"))
                .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/venues"))
            .andExpect(flash().attribute("errorMessage", "Venue not found."));
    }
}