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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.entities.Event;
import jakarta.servlet.http.HttpServletRequest;

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
			LocalDate today = LocalDate.now();
			Iterable<Event> searchResults = eventService.search(search.trim());

			List<Event> upcomingEvents = new ArrayList<>();
			List<Event> pastEvents = new ArrayList<>();

			for (Event event : searchResults) {
				if (event.getDate().isBefore(today)) {
					pastEvents.add(event);
				} else {
					upcomingEvents.add(event);
				}
			}

			model.addAttribute("upcomingEvents", upcomingEvents);
			model.addAttribute("pastEvents", pastEvents);
			model.addAttribute("searchTerm", search);
		} else {
			LocalDate today = LocalDate.now();
			model.addAttribute("upcomingEvents", eventService.findUpcomingEvents(today));
			model.addAttribute("pastEvents", eventService.findPastEvents(today));
			model.addAttribute("events", eventService.findAll());
		}
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}

	@GetMapping("/edit/{id}")
	public String showUpdateForm(@PathVariable("id") long id,
			@RequestParam(value = "redirect", required = false) String redirect,
			Model model,
			Principal principal,
			HttpServletRequest request) {
		return eventService.findById(id).map(event -> {
			if (!event.getOwnerUsername().equals(principal.getName()) && !request.isUserInRole("ADMINISTRATOR")) {
				return "redirect:/events";
			}

			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("redirect", redirect);
			return "events/update";
		}).orElseThrow(() -> new EventNotFoundException(id));
	}

	@PostMapping("/update/{id}")
	public String updateEvent(@PathVariable("id") long id,
			@Valid @ModelAttribute("event") Event updatedEvent,
			BindingResult result,
			@RequestParam(value = "redirect", required = false) String redirect,
			Model model,
			Principal principal,
			HttpServletRequest request) {

		if (result.hasErrors()) {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("redirect", redirect);
			return "events/update";
		}

		return eventService.findById(id).map(existingEvent -> {
			if (!existingEvent.getOwnerUsername().equals(principal.getName())
					&& !request.isUserInRole("ADMINISTRATOR")) {
				return "redirect:/events";
			}

			existingEvent.setName(updatedEvent.getName());
			existingEvent.setDate(updatedEvent.getDate());
			existingEvent.setTime(updatedEvent.getTime());
			existingEvent.setVenue(updatedEvent.getVenue());
			existingEvent.setDescription(updatedEvent.getDescription());

			eventService.save(existingEvent);

			if (redirect != null && !redirect.isBlank()) {
				return "redirect:" + redirect;
			}

			return "redirect:/events";
		}).orElseThrow(() -> new EventNotFoundException(id));
	}

}
