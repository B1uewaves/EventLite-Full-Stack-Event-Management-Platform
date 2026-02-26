package uk.ac.man.cs.eventlite.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/my/events", produces = { MediaType.TEXT_HTML_VALUE })
public class MyEventsController {

	@Autowired
	private EventService eventService;

	@GetMapping
	public String index(Model model, Principal principal) {
		String username = principal.getName();
		model.addAttribute("events", eventService.findAllForUser(username));
		return "my/events/index";
	}

	@GetMapping("/new")
	public String newEvent(Model model) {
		model.addAttribute("event", new Event());
		return "my/events/new";
	}

	@PostMapping
	public String createEvent(Event event, Principal principal) {
		eventService.saveForUser(event, principal.getName());
		return "redirect:/my/events";
	}
}