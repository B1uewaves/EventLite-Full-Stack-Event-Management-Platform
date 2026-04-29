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

	private static final String OWNER = "Markel";

	@Bean
	CommandLineRunner initDatabase(EventService eventService, VenueService venueService) {
		return args -> {
			if (eventService.count() > 0) {
				log.info("Database already populated. Skipping initial data load.");
				return;
			}

			Venue venueA = new Venue();
			venueA.setName("Venue A");
			venueA.setAddress("Royal Observatory, Greenwich, London");
			venueA.setPostcode("SE10 9NF");
			venueA.setCapacity(200);

			Venue venueB = new Venue();
			venueB.setName("Venue B");
			venueB.setAddress("Old Market Square, Nottingham");
			venueB.setPostcode("NG1 2DT");
			venueB.setCapacity(150);

			venueService.save(venueA);
			venueService.save(venueB);

			eventService.save(buildEvent("Event Alpha", LocalDate.of(2026, 7, 11), LocalTime.of(12, 30), venueB));
			eventService.save(buildEvent("Event Beta",  LocalDate.of(2026, 7, 11), LocalTime.of(10,  0), venueA));
			eventService.save(buildEvent("Event Apple", LocalDate.of(2026, 7, 12), null,                 venueA));

			eventService.save(buildEvent("Event Former",   LocalDate.of(2020, 1, 11), LocalTime.of(11,  0), venueB));
			eventService.save(buildEvent("Event Previous", LocalDate.of(2020, 1, 11), LocalTime.of(18, 30), venueA));
			eventService.save(buildEvent("Event Past",     LocalDate.of(2020, 1, 10), LocalTime.of(17,  0), venueA));

			log.info("Seeded 2 venues and 6 events from the specification.");
		};
	}

	private static Event buildEvent(String name, LocalDate date, LocalTime time, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDate(date);
		event.setTime(time);
		event.setVenue(venue);
		event.setOwnerUsername(OWNER);
		return event;
	}
}
