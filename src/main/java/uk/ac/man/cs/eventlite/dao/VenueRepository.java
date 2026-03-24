package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.JpaRepository; // do we need this? why cant we use a crud repository 
import org.springframework.stereotype.Repository;
import uk.ac.man.cs.eventlite.entities.Venue;

import org.springframework.data.repository.CrudRepository;


@Repository
public interface VenueRepository extends CrudRepository<Venue, Long> {
    Iterable<Venue> findByNameContainingIgnoreCase(String name);
    Iterable<Venue> findAllByOrderByCapacityDesc();
}