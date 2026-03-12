package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import uk.ac.man.cs.eventlite.entities.Event;

import uk.ac.man.cs.eventlite.dao.EventService;
import org.springframework.ui.Model;



@Controller
public class HomeController {

    @Autowired 
    private EventService EventService;



    
    @GetMapping("/")
    public String home(Model model){
        LocalDate today = LocalDate.now();
        List<Event> upcomingThree = new ArrayList<>();
        Iterable<Event> upcomingEvents = EventService.findUpcomingEvents(today);

        // iterator used so i can use .next()
        Iterator<Event> upcomingEventsIterator = upcomingEvents.iterator();

        int count = 0;
        while (upcomingEventsIterator.hasNext() && count < 3) {
            upcomingThree.add(upcomingEventsIterator.next());
            count++;
        }

        model.addAttribute("upcomingThree", upcomingThree);
        return "home/index";

    }

}