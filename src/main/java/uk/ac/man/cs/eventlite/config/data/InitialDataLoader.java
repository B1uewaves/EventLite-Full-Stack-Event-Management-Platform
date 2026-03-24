package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private static final Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Bean
	CommandLineRunner initDatabase(EventService eventService, VenueService venueService) {
		return args -> {
			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
				return;
			}

			Event e1 = new Event();
			e1.setName("Distributed Systems Seminar");
			e1.setDate(LocalDate.of(2026, 5, 12));
			e1.setTime(LocalTime.of(9, 0));
			e1.setOwnerUsername("Alice");
			Venue v1 = new Venue();
			v1.setName("Whitworth Hall");
			v1.setAddress("Oxford Road, Manchester");
			v1.setPostcode("M13 9PL");
			v1.setCapacity(500);
			e1.setVenue(v1);

			Event e2 = new Event();
			e2.setName("Machine Learning Workshop");
			e2.setDate(LocalDate.of(2026, 5, 12));
			e2.setTime(LocalTime.of(12, 0));
			e2.setOwnerUsername("Tom");
			Venue v2 = new Venue();
			v2.setName("Alan Turing");
			v2.setAddress("Upper Brook Street, Manchester");
			v2.setPostcode("M13 9PL");
			v2.setCapacity(1000);
			e2.setVenue(v2);

			Event e3 = new Event();
			e3.setName("Software Engineering Project Showcase");
			e3.setDate(LocalDate.of(2026, 5, 13));
			e3.setTime(LocalTime.of(10, 0));
			e3.setOwnerUsername("Caroline");
			Venue v3 = new Venue();
			v3.setName("Crawford House");
			v3.setAddress("Booth Street East, Manchester");
			v3.setPostcode("M13 9SS");
			v3.setCapacity(5000);
			e3.setVenue(v3);

			venueService.save(v1);
			venueService.save(v2);
			venueService.save(v3);
			eventService.save(e1);
			eventService.save(e2);
			eventService.save(e3);
		};
	}
}

