package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventsControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;

	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<String> eventNotFoundHandler(EventNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.contentType(MediaType.APPLICATION_JSON)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Event> getEvent(@PathVariable("id") long id) {
		Event event = eventService.findById(id)
				.orElseThrow(() -> new EventNotFoundException(id));

		return eventAssembler.toModel(event);
	}

	@GetMapping
	public CollectionModel<EntityModel<Event>> getAllEvents() {
		List<EntityModel<Event>> events = StreamSupport
				.stream(eventService.findAll().spliterator(), false)
				.map(eventAssembler::toModel)
				.toList();
	
		return CollectionModel.of(events,
				linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel());
	}
}