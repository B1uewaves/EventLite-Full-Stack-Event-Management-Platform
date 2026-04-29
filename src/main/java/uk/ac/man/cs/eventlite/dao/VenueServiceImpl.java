package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	private VenueRepository venueRepository;

	@Autowired // service has an repository, service does not implement or extend a repository
	public VenueServiceImpl(VenueRepository venueRepository) {
		this.venueRepository = venueRepository;
	}

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAllByOrderByNameAsc();
	}

	@Override
	public Venue save(Venue venue) {
		String normalizedName = venue.getName().trim().replaceAll("\\s+", " ");
	    venue.setName(normalizedName);
		return venueRepository.save(venue);
	}

	@Override
	public Iterable<Venue> search(String name) {
		return venueRepository.findByNameContainingIgnoreCase(name);
	}

	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
	}	

	@Override
	public Iterable<Venue> popularVenues(){
		return venueRepository.findAllByOrderByCapacityDesc();
	}

	
	@Override
	public boolean existsByName(String name) {
	    String normalizedName = name.trim().replaceAll("\\s+", " ");
	    return venueRepository.existsByNameIgnoreCase(normalizedName);
	}
	
	@Override
	public void deleteById(long id) {
	    venueRepository.deleteById(id);
	}
}
