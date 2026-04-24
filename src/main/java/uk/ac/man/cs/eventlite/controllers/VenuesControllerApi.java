package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping("/api/venues")
public class VenuesControllerApi {

    private final VenueService venueService;
    private final EventService eventService;
    private final VenueModelAssembler venueModelAssembler;
    private final EventModelAssembler eventModelAssembler;

    public VenuesControllerApi(VenueService venueService,
                               EventService eventService,
                               VenueModelAssembler venueModelAssembler,
                               EventModelAssembler eventModelAssembler) {
        this.venueService = venueService;
        this.eventService = eventService;
        this.venueModelAssembler = venueModelAssembler;
        this.eventModelAssembler = eventModelAssembler;
    }

    @GetMapping("/{id}")
    public EntityModel<Venue> getVenue(@PathVariable long id) {
        System.out.println(">>> getVenue called with id = " + id);

        Venue venue = venueService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        return venueModelAssembler.toModel(venue);
    }

    @GetMapping("/{id}/events")
    public CollectionModel<EntityModel<Event>> getVenueEvents(@PathVariable long id) {
        Venue venue = venueService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        List<EntityModel<Event>> events = venue.getEvents().stream()
                .map(eventModelAssembler::toModel)
                .toList();

        return CollectionModel.of(
                events,
                linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(id)).withSelfRel()
        );
    }

    @GetMapping("/{id}/next3events")
    public CollectionModel<EntityModel<Event>> getNext3Events(@PathVariable long id) {
        venueService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        Iterable<Event> upcoming = eventService.findNext3EventsByVenueId(id, LocalDate.now());

        List<EntityModel<Event>> events = new ArrayList<>();
        for (Event event : upcoming) {
            events.add(eventModelAssembler.toModel(event));
        }

        return CollectionModel.of(
                events,
                linkTo(methodOn(VenuesControllerApi.class).getNext3Events(id)).withSelfRel()
        );
    }
    
    @GetMapping
    public CollectionModel<EntityModel<Venue>> getAllVenues() {

        List<EntityModel<Venue>> venues = new ArrayList<>();

        for (Venue venue : venueService.findAll()) {
            venues.add(venueModelAssembler.toModel(venue));
        }

        return CollectionModel.of(
            venues,
            linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel(),
            Link.of("http://localhost:8080/api/profile/venues", "profile")

        );
    }
}