package uk.ac.man.cs.eventlite.config.data;

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
				// Build and save initial events here.
			}
		};
	}
}
