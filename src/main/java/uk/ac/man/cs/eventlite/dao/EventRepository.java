package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

	Iterable<Event> findAllByOrderByDateAscTimeAsc();

	Iterable<Event> findByOwnerUsernameOrderByDateAscTimeAsc(String ownerUsername);

	Iterable<Event> findByNameContainingIgnoreCase(String name);

	Iterable<Event> findByDateBefore(LocalDate date);

	Iterable<Event> findByDateGreaterThanEqual(LocalDate date);
    
    List<Event> findTop3ByVenueIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(long venueId, LocalDate date);
    
}
