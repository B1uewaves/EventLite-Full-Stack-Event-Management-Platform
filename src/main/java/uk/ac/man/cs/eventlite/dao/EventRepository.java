package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
<<<<<<< HEAD
=======
import java.time.LocalDate;

import java.util.List;
>>>>>>> refs/remotes/origin/venueapi

public interface EventRepository extends CrudRepository<Event, Long> {

<<<<<<< HEAD
	Iterable<Event> findAllByOrderByDateAscTimeAsc();

	Iterable<Event> findByOwnerUsernameOrderByDateAscTimeAsc(String ownerUsername);

	Iterable<Event> findByNameContainingIgnoreCase(String name);

	Iterable<Event> findByDateBeforeOrderByDateDescTimeDesc(LocalDate date);

	Iterable<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
}
=======
    Iterable<Event> findByDateBeforeOrderByDateDescTimeDesc(LocalDate date);
    Iterable<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
    
    List<Event> findTop3ByVenueIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(long venueId, LocalDate date);
}
>>>>>>> refs/remotes/origin/venueapi
