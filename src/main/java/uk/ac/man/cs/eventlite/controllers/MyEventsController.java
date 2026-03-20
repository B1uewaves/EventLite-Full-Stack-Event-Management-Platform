package uk.ac.man.cs.eventlite.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

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
	public String createEvent(Event event, Principal principal) {
		eventService.saveForUser(event, principal.getName());
		return "redirect:/my/events";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id,
	                          RedirectAttributes redirectAttrs) {

	    eventService.deleteById(id);
	    redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");

	    return "redirect:/my/events";
	}
}