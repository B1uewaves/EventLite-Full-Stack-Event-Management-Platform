package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.entities.Event;

import java.security.Principal;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	@Autowired
	private VenueService venueService;





	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {

		return eventService.findById(id).map(event -> {
			model.addAttribute("event", event);
			return "events/show";
		}).orElseThrow(() -> new EventNotFoundException(id));
	}

	@GetMapping
	public String getAllEvents(@RequestParam(value = "search", required = false) String search, Model model) {
		if (search != null && !search.trim().isEmpty()) {
			model.addAttribute("events", eventService.search(search.trim()));
			model.addAttribute("searchTerm", search);
		} else {
			model.addAttribute("events", eventService.findAll());
		}
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}

	@GetMapping("/edit/{id}")
	public String showUpdateForm(@PathVariable("id") long id, Model model, Principal principal) {
	    return eventService.findById(id).map(event -> {
	        if (!event.getOwnerUsername().equals(principal.getName())) {
	            return "redirect:/events";
	        }

	        model.addAttribute("event", event);
	        model.addAttribute("venues", venueService.findAll());
	        return "events/update";
	    }).orElseThrow(() -> new EventNotFoundException(id));
	}

	@PostMapping("/update/{id}")
	public String updateEvent(@PathVariable("id") long id,
	                          @Valid @ModelAttribute("event") Event updatedEvent,
	                          BindingResult result,
	                          Model model,
	                          Principal principal) {

	    if (result.hasErrors()) {
	        model.addAttribute("venues", venueService.findAll());
	        return "events/update";
	    }

	    return eventService.findById(id).map(existingEvent -> {
	        if (!existingEvent.getOwnerUsername().equals(principal.getName())) {
	            return "redirect:/events";
	        }

	        existingEvent.setName(updatedEvent.getName());
	        existingEvent.setDate(updatedEvent.getDate());
	        existingEvent.setTime(updatedEvent.getTime());
	        existingEvent.setVenue(updatedEvent.getVenue());

	        eventService.save(existingEvent);
	        return "redirect:/events";
	    }).orElseThrow(() -> new EventNotFoundException(id));
	}

}
