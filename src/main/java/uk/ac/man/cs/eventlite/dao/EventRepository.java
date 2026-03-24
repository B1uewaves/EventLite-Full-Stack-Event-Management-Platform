package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {

	Iterable<Event> findAllByOrderByDateAscTimeAsc();

	Iterable<Event> findByOwnerUsernameOrderByDateAscTimeAsc(String ownerUsername);

	Iterable<Event> findByNameContainingIgnoreCase(String name);

	Iterable<Event> findByDateBeforeOrderByDateDescTimeDesc(LocalDate date);

	Iterable<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
}