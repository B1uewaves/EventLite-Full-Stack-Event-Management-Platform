package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.man.cs.eventlite.entities.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    Iterable<Venue> findByNameContainingIgnoreCase(String name);
    Iterable<Venue> findAllByOrderByCapacityDesc();
    
    boolean existsByNameIgnoreCase(String name);
}