package uk.ac.man.cs.eventlite.dao;
import java.time.LocalDate;
import java.util.Optional;
import uk.ac.man.cs.eventlite.entities.Event;
import java.time.LocalDate;

import java.util.Optional;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();

	public Event save(Event event);
	
	public Optional<Event> findById(long id);
	
	// --- Organiser (logged-in user) ---
	public Iterable<Event> findAllForUser(String ownerUsername);

	public Event saveForUser(Event event, String ownerUsername);

	public Iterable<Event> search(String name);

    public Iterable<Event> findPastEvents(LocalDate currentDate);
    public Iterable<Event> findUpcomingEvents(LocalDate currentDate);
	
}
