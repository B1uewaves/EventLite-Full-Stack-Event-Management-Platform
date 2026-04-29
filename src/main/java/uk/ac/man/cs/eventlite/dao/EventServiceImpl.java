package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.StreamSupport;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	@Autowired
	private EventRepository eventRepository;
	
	@Autowired
	public EventServiceImpl(EventRepository eventRepository) {
	    this.eventRepository = eventRepository;
	}

	@Override
	public long count() {
		return eventRepository.count();
	}


	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}

	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	
	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override
	public Iterable<Event> findAllForUser(String ownerUsername) {
		return eventRepository.findByOwnerUsernameOrderByDateAscTimeAsc(ownerUsername);
	}

	@Override
	public Event saveForUser(Event event, String ownerUsername) {
		event.setOwnerUsername(ownerUsername);
		return eventRepository.save(event);
	}

	@Override
	public Iterable<Event> search(String name) {
		if (name == null || name.trim().isEmpty()) {
			return eventRepository.findAll();
		}

		String searchTerm = name.trim().toLowerCase();

		return StreamSupport.stream(eventRepository.findAll().spliterator(), false)
			.filter(event -> Arrays.stream(event.getName().toLowerCase().split("[^a-z0-9]+"))
				.anyMatch(word -> word.equals(searchTerm)))
			.sorted(
				Comparator.comparing(Event::getDate)
					.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER)
			)
			.toList();
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public Iterable<Event> findPastEvents(LocalDate currentDate) {
		return StreamSupport.stream(eventRepository.findByDateBefore(currentDate).spliterator(), false)
			.sorted(
				Comparator.comparing(Event::getDate).reversed()
					.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER)
			)
			.toList();
	}

	@Override
	public Iterable<Event> findUpcomingEvents(LocalDate currentDate) {
		return StreamSupport.stream(eventRepository.findByDateGreaterThanEqual(currentDate).spliterator(), false)
			.sorted(
				Comparator.comparing(Event::getDate)
					.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER)
			)
			.toList();
	}
	
	@Override
	public Iterable<Event> findNext3EventsByVenueId(long venueId, LocalDate currentDate) {
	    return eventRepository.findTop3ByVenueIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(venueId, currentDate);
	}
}

