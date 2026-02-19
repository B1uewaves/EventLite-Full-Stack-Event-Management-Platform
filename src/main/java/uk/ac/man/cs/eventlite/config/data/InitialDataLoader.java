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
import uk.ac.man.cs.eventlite.entities.Venue;
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
				Venue v1 = new Venue();
				v1.setName("Withworth Hall");
				v1.setCapacity(500);
				
				Venue v2 = new Venue();
				v2.setName("Alan Turing");
				v2.setCapacity(1000);
				
				Venue v3 = new Venue();
				v3.setName("Crawford House");
				v3.setCapacity(5000);
				
				venueService.save(v1);
				venueService.save(v2);
				venueService.save(v3);
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				//events that were previously in events.json
                Event e1 = new Event();
                e1.setName("COMP23412 Showcase 01");
                e1.setVenue(1);
                e1.setDate(LocalDate.of(2026, 5, 12));
                e1.setTime(LocalTime.of(9, 0));


                Event e2 = new Event();
                e2.setName("COMP23412 Showcase 02");
                e2.setVenue(1);
                e2.setDate(LocalDate.of(2026, 5, 12));
                e2.setTime(LocalTime.of(12, 0));

                Event e3 = new Event();
                e3.setName("COMP23412 Showcase 03");
                e3.setVenue(1);
                e3.setDate(LocalDate.of(2026, 5, 13));
                e3.setTime(LocalTime.of(10, 0));
				
                eventService.save(e1);
                eventService.save(e2);
                eventService.save(e3);
			}
		};
	}
}
