package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDate;



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
		return eventRepository.findByNameContainingIgnoreCase(name);
	}


	@Override
	public Iterable<Event> findPastEvents(LocalDate currentDate) {
		return eventRepository.findByDateBeforeOrderByDateDescTimeDesc(currentDate);
	}

	@Override
	public Iterable<Event> findUpcomingEvents(LocalDate currentDate) {
		return eventRepository.findByDateGreaterThanEqualOrderByDateAscTimeAsc(currentDate);
	}
}
