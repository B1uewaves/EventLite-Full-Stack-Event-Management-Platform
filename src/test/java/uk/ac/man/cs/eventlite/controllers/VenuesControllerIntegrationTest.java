package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private VenueRepository venueRepository;

	@Test
	public void getAllVenuesPassesVenuesToModel() throws Exception {
		mvc.perform(get("/venues").accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/index"))
				.andExpect(model().attributeExists("venues"))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", equalTo("Alan Turing")))))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", equalTo("Crawford House")))))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", equalTo("Withworth Hall")))))
				.andExpect(content().string(containsString("All Venues")));
	}

	@Test
	public void searchVenuesPassesResultsAndSearchTermToModel() throws Exception {
		mvc.perform(get("/venues").param("search", "Alan").accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/index"))
				.andExpect(model().attribute("searchTerm", "Alan"))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", equalTo("Alan Turing")))))
				.andExpect(content().string(containsString("Alan Turing")))
				.andExpect(content().string(not(containsString("Crawford House"))));
	}

	@Test
	public void searchVenuesWithNoMatchPassesEmptyResultsAndSearchTermToModel() throws Exception {
		mvc.perform(get("/venues").param("search", "ZZNothingHere").accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/index"))
				.andExpect(model().attribute("searchTerm", "ZZNothingHere"))
				.andExpect(model().attributeExists("venues"))
				.andExpect(content().string(containsString("No venues found matching")))
				.andExpect(content().string(containsString("ZZNothingHere")));
	}

	@Test
	public void getSingleVenuePassesVenueToModel() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Test Venue", 321));

		mvc.perform(get("/venues/" + savedVenue.getId()).accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/show"))
				.andExpect(model().attribute("venue", hasProperty("name", equalTo("Integration Test Venue"))))
				.andExpect(model().attribute("venue", hasProperty("address", equalTo("Oxford Road, Manchester"))))
				.andExpect(model().attribute("venue", hasProperty("postcode", equalTo("M13 9PL"))))
				.andExpect(model().attribute("venue", hasProperty("capacity", equalTo(321))))
				.andExpect(content().string(containsString("Integration Test Venue")));
	}

	@Test
	public void getVenueNotFoundPassesMissingIdToModel() throws Exception {
		mvc.perform(get("/venues/99999").accept("text/html"))
				.andExpect(status().isNotFound())
				.andExpect(view().name("venues/not_found"))
				.andExpect(model().attribute("not_found_id", 99999L))
				.andExpect(content().string(containsString("Venue Not Found")))
				.andExpect(content().string(containsString("99999")));
	}

	@Test
	public void getEditVenuePassesVenueToModel() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Edit Venue", 222));

		mvc.perform(get("/venues/edit/" + savedVenue.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attribute("venue", hasProperty("name", equalTo("Integration Edit Venue"))))
				.andExpect(model().attribute("venue", hasProperty("address", equalTo("Oxford Road, Manchester"))))
				.andExpect(model().attribute("venue", hasProperty("postcode", equalTo("M13 9PL"))))
				.andExpect(model().attribute("venue", hasProperty("capacity", equalTo(222))));
	}

	@Test
	public void postAddVenueWithSensibleDataSavesVenueAndRedirects() throws Exception {
		mvc.perform(post("/venues/add")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Add Venue")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "M13 9PL")
				.param("capacity", "250")
				.accept("text/html"))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(redirectedUrl("/venues"));

		assertTrue(venueRepository.existsByNameIgnoreCase("Integration Add Venue"));
	}

	@Test
	public void postAddVenueWithMissingDataReturnsFormWithModelErrors() throws Exception {
		mvc.perform(post("/venues/add")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "M13 9PL")
				.param("capacity", "250")
				.accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"));
	}

	@Test
	public void postAddVenueWithInvalidDataReturnsFormWithModelErrors() throws Exception {
		mvc.perform(post("/venues/add")
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Invalid Venue")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "not-a-postcode")
				.param("capacity", "250")
				.accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/add"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "postcode"));
	}

	@Test
	public void postAddVenueAsUnauthorisedUserRedirectsToSignIn() throws Exception {
		mvc.perform(post("/venues/add")
				.with(csrf())
				.param("name", "Unauthorised Venue")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "M13 9PL")
				.param("capacity", "250")
				.accept("text/html"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("**/sign-in"));
	}

	@Test
	public void postUpdateVenueWithSensibleDataUpdatesVenueAndRedirects() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Update Venue", 100));

		mvc.perform(post("/venues/update/" + savedVenue.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Updated Integration Venue")
				.param("address", "Sackville Street, Manchester")
				.param("postcode", "M1 3BB")
				.param("capacity", "450")
				.accept("text/html"))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues"))
				.andExpect(redirectedUrl("/venues"));

		Venue updatedVenue = venueRepository.findById(savedVenue.getId()).orElseThrow();
		assertEquals("Updated Integration Venue", updatedVenue.getName());
		assertEquals("Sackville Street, Manchester", updatedVenue.getAddress());
		assertEquals("M1 3BB", updatedVenue.getPostcode());
		assertEquals(450, updatedVenue.getCapacity());
	}

	@Test
	public void postUpdateVenueWithMissingDataReturnsFormWithModelErrors() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Missing Update Venue", 100));

		mvc.perform(post("/venues/update/" + savedVenue.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Missing Update Venue")
				.param("address", "")
				.param("postcode", "M13 9PL")
				.param("capacity", "100")
				.accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "address"));
	}

	@Test
	public void postUpdateVenueWithInvalidDataReturnsFormWithModelErrors() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Invalid Update Venue", 100));

		mvc.perform(post("/venues/update/" + savedVenue.getId())
				.with(user("Alice").roles("ADMINISTRATOR"))
				.with(csrf())
				.param("name", "Integration Invalid Update Venue")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "M13 9PL")
				.param("capacity", "-10")
				.accept("text/html"))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venue"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"));
	}

	@Test
	public void postUpdateVenueAsUnauthorisedUserRedirectsToSignIn() throws Exception {
		Venue savedVenue = venueRepository.save(venue("Integration Unauthorised Update Venue", 100));

		mvc.perform(post("/venues/update/" + savedVenue.getId())
				.with(csrf())
				.param("name", "Unauthorised Update Venue")
				.param("address", "Oxford Road, Manchester")
				.param("postcode", "M13 9PL")
				.param("capacity", "100")
				.accept("text/html"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("**/sign-in"));
	}

	private Venue venue(String name, int capacity) {
		Venue venue = new Venue();
		venue.setName(name);
		venue.setAddress("Oxford Road, Manchester");
		venue.setPostcode("M13 9PL");
		venue.setCapacity(capacity);
		return venue;
	}
}
