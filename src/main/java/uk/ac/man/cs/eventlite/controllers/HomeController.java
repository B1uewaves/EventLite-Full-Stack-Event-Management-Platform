package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.services.MastodonService;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import org.springframework.ui.Model;



@Controller
public class HomeController {

    @Autowired
    private EventService EventService;

    @Autowired
    VenueService VenueService;
    
    @Autowired
    private MastodonService mastodonService;
    
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(Model model){
        LocalDate today = LocalDate.now();
        List<Event> upcomingThree = new ArrayList<>();
        List<Venue> popularThree = new ArrayList<>();

        Iterable<Venue> popularVenues = VenueService.popularVenues();
        Iterable<Event> upcomingEvents = EventService.findUpcomingEvents(today);

        Iterator<Event> upcomingEventsIterator = upcomingEvents.iterator();
        Iterator<Venue> popularVenuesIterator = popularVenues.iterator();

        int Ecount = 0;
        while (upcomingEventsIterator.hasNext() && Ecount < 3) {
            upcomingThree.add(upcomingEventsIterator.next());
            Ecount++;
        }

        int Vcount = 0;
        while (popularVenuesIterator.hasNext() && Vcount < 3) {
            popularThree.add(popularVenuesIterator.next());
            Vcount++;
        }

        model.addAttribute("upcomingThree", upcomingThree);
        model.addAttribute("popularThree", popularThree);
        model.addAttribute("posts", mastodonService.getLatestPosts());


        return "home/index";
    }
}