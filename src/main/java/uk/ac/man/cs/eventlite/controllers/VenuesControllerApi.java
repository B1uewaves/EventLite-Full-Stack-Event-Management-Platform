package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = MediaType.APPLICATION_JSON_VALUE)
public class VenuesControllerApi {

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueModelAssembler venueAssembler;

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<String> venueNotFoundHandler(VenueNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(String.format("{ \"error\": \"Could not find venue\", \"id\": %d }", ex.getId()));
    }

    @GetMapping("/{id}")
    public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));

        return venueAssembler.toModel(venue);
    }

    @GetMapping
    public CollectionModel<EntityModel<Venue>> getAllVenues() {
        List<EntityModel<Venue>> venues = StreamSupport
                .stream(venueService.findAll().spliterator(), false)
                .map(venueAssembler::toModel)
                .toList();

        return CollectionModel.of(venues,
                linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel());
    }
}