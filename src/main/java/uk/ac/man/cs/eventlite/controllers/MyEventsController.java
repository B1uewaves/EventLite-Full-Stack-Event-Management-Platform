package uk.ac.man.cs.eventlite.controllers;

import java.security.Principal;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import jakarta.validation.Valid;

@Controller
@RequestMapping(value = "/my/events", produces = { MediaType.TEXT_HTML_VALUE })
public class MyEventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

	@GetMapping
	public String index(Model model, Principal principal) {
		String username = principal.getName();
		model.addAttribute("events", eventService.findAllForUser(username));
		return "my/events/index";
	}

	@GetMapping("/new")
	public String newEvent(Model model) {
		model.addAttribute("event", new Event());
		model.addAttribute("venues", venueService.findAll());
		return "my/events/new";
	}



	@PostMapping
	public String createEvent(@Valid @ModelAttribute("event") Event event,
	                          BindingResult result,
	                          Principal principal,
	                          Model model) {

	    if (result.hasErrors()) {
	        model.addAttribute("venues", venueService.findAll());
	        return "my/events/new";
	    }

	    eventService.saveForUser(event, principal.getName());
	    return "redirect:/my/events";
	}
}