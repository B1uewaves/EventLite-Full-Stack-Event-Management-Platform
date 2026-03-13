package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(Security.class)
class HomeControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private EventService eventService;

  @MockitoBean
  private VenueService venueService;

  @Test
  void getHomeWithNoData() throws Exception {
    // when calling find upcoming events in event service it just returns an empty list
    // similiar thing happens for popularvenues
    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(Collections.emptyList());
    when(venueService.popularVenues()).thenReturn(Collections.emptyList());


    // mock http get request at url "/" it also tells the server it wants a HTML webpage (not JSON data) 
    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk()) // checks for any 404 errors 
        .andExpect(view().name("home/index")) //checks if home/index is displayed
        .andExpect(handler().methodName("home")) // checks if name of func called is home
        .andExpect(model().attributeExists("upcomingThree"))
        .andExpect(model().attributeExists("popularThree"))
        .andExpect(model().attribute("upcomingThree", hasSize(0)))
        .andExpect(model().attribute("popularThree", hasSize(0)));

    //checks that the controler actually called findUcpomingEvents and popularVenues
    verify(eventService).findUpcomingEvents(any(LocalDate.class));
    verify(venueService).popularVenues();
  }



  @Test
  void getHomeLimitsBothListsToThree() throws Exception {
    List<Event> events = Arrays.asList(
        Mockito.mock(Event.class), Mockito.mock(Event.class), Mockito.mock(Event.class),
        Mockito.mock(Event.class), Mockito.mock(Event.class));

    List<Venue> venues = Arrays.asList(
        Mockito.mock(Venue.class), Mockito.mock(Venue.class),
        Mockito.mock(Venue.class), Mockito.mock(Venue.class));
    
    // events is a list of events that have 4 mock event objects 
    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(events);
    when(venueService.popularVenues()).thenReturn(venues);

    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("home/index"))
        .andExpect(model().attribute("upcomingThree", hasSize(3)))
        .andExpect(model().attribute("popularThree", hasSize(3)));
  }

    @Test
    void getHomeWithLessThanThreeKeepsOriginalSize() throws Exception {
    List<Event> events = Arrays.asList(Mockito.mock(Event.class), Mockito.mock(Event.class));
    List<Venue> venues = Collections.singletonList(Mockito.mock(Venue.class));

    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(events);
    when(venueService.popularVenues()).thenReturn(venues);

    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(model().attribute("upcomingThree", hasSize(2)))
        .andExpect(model().attribute("popularThree", hasSize(1)));
    }

    @Test
    void getHomePassesTodayToUpcomingQuery() throws Exception {
    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(Collections.emptyList());
    when(venueService.popularVenues()).thenReturn(List.of());

    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk());

    verify(eventService).findUpcomingEvents(LocalDate.now());
    }

    @Test
    void getHomeKeepsFirstThreeUpcomingEventsInOrder() throws Exception {
    Event e1 = new Event(); 
    e1.setDate(LocalDate.of(2026, 3, 14)); 
    
    e1.setName("E1");
    Event e2 = new Event(); 
    e2.setDate(LocalDate.of(2026, 3, 15)); 
    e2.setName("E2");
    Event e3 = new Event(); 
    e3.setDate(LocalDate.of(2026, 3, 16)); 
    e3.setName("E3");
    Event e4 = new Event(); 
    e4.setDate(LocalDate.of(2026, 3, 17)); 
    e4.setName("E4");

    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(Arrays.asList(e1, e2, e3, e4));
    when(venueService.popularVenues()).thenReturn(List.of());

    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(result -> {
            @SuppressWarnings("unchecked") 
            // this just tells the compiler to stop the warning and cast the object (form the spring model) into a list of events not needed but just makes the terminal look clean
            List<Event> upcoming = (List<Event>) result.getModelAndView().getModel().get("upcomingThree");
            assertEquals(3, upcoming.size());
            assertEquals("E1", upcoming.get(0).getName());
            assertEquals("E2", upcoming.get(1).getName());
            assertEquals("E3", upcoming.get(2).getName());
        });
    }

    @Test
    void getHomeKeepsFirstThreePopularVenuesInOrder() throws Exception {
    Venue v1 = new Venue(); 
    v1.setName("V1"); 
    v1.setCapacity(400);
    Venue v2 = new Venue(); 
    v2.setName("V2"); 
    v2.setCapacity(300);
    Venue v3 = new Venue(); 
    v3.setName("V3"); 
    v3.setCapacity(200);
    Venue v4 = new Venue();
    v4.setName("V4"); 
    v4.setCapacity(100);

    when(eventService.findUpcomingEvents(any(LocalDate.class))).thenReturn(List.of());
    when(venueService.popularVenues()).thenReturn(Arrays.asList(v1, v2, v3, v4));

    mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(result -> {
            @SuppressWarnings("unchecked") 
            List<Venue> popular = (List<Venue>) result.getModelAndView().getModel().get("popularThree");
            assertEquals(3, popular.size());
            assertEquals("V1", popular.get(0).getName());
            assertEquals("V2", popular.get(1).getName());
            assertEquals("V3", popular.get(2).getName());
        });
    }


}