package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("test")
public class TestDataLoader {

	private final static Logger log = LoggerFactory.getLogger(TestDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
	    return args -> {
	        Venue v1 = new Venue();
	        v1.setName("Withworth Hall");
	        v1.setCapacity(500);

	        Venue v2 = new Venue();
	        v2.setName("Alan Turing");
	        v2.setCapacity(1000);

	        Venue v3 = new Venue();
	        v3.setName("Crawford House");
	        v3.setCapacity(5000);

	        Venue venue = new Venue();
	        venue.setName("Test Venue");
	        venue.setCapacity(100);

	        venueService.save(v1);
	        venueService.save(v2);
	        venueService.save(v3);
	        venueService.save(venue);

	        Event e1 = new Event();
	        e1.setName("InitialDataLoader creation 01");
	        e1.setDate(LocalDate.of(2026, 5, 12));
	        e1.setTime(LocalTime.of(9, 0));
	        e1.setOwnerUsername("Alice");
	        e1.setVenue(v1);

	        Event e2 = new Event();
	        e2.setName("InitialDataLoader creation 02");
	        e2.setDate(LocalDate.of(2026, 5, 12));
	        e2.setTime(LocalTime.of(12, 0));
	        e2.setOwnerUsername("Tom");
	        e2.setVenue(v2);

	        Event e3 = new Event();
	        e3.setName("InitialDataLoader creation 03");
	        e3.setDate(LocalDate.of(2026, 5, 13));
	        e3.setTime(LocalTime.of(10, 0));
	        e3.setOwnerUsername("Caroline");
	        e3.setVenue(v3);

	        Event event1 = new Event();
	        event1.setName("Delete Test Event");
	        event1.setDate(LocalDate.of(2026, 5, 1));
	        event1.setTime(LocalTime.of(10, 0));
	        event1.setVenue(venue);
	        event1.setDescription("Event used for delete acceptance test");
	        event1.setOwnerUsername("Rob");

	        Event event2 = new Event();
	        event2.setName("Second Test Event");
	        event2.setDate(LocalDate.of(2026, 5, 2));
	        event2.setTime(LocalTime.of(11, 0));
	        event2.setVenue(venue);
	        event2.setDescription("Another event");
	        event2.setOwnerUsername("Alice");

	        Event event3 = new Event();
	        event3.setName("Third Test Event");
	        event3.setDate(LocalDate.of(2026, 5, 3));
	        event3.setTime(LocalTime.of(12, 0));
	        event3.setVenue(venue);
	        event3.setDescription("Third seeded event");
	        event3.setOwnerUsername("Rob");

	        eventService.save(e1);
	        eventService.save(e2);
	        eventService.save(e3);
	        eventService.save(event1);
	        eventService.save(event2);
	        eventService.save(event3);

	        log.info("Loaded test events and venues.");
	    };
	}
}
