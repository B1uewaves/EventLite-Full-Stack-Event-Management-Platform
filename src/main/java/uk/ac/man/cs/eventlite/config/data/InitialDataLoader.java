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

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				//events that were previously in events.json
                Event event1 = new Event();
                event1.setName("COMP23412 Showcase 01");
                event1.setVenue(1);
                event1.setDate(LocalDate.of(2026, 5, 12));
                event1.setTime(LocalTime.of(9, 0));
                eventService.save(event1);

                Event event2 = new Event();
                event2.setName("COMP23412 Showcase 02");
                event2.setVenue(1);
                event2.setDate(LocalDate.of(2026, 5, 12));
                event2.setTime(LocalTime.of(12, 0));
                eventService.save(event2);

                Event event3 = new Event();
                event3.setName("COMP23412 Showcase 03");
                event3.setVenue(1);
                event3.setDate(LocalDate.of(2026, 5, 13));
                event3.setTime(LocalTime.of(10, 0));
				eventService.save(event3);
			}
		};
	}
}
