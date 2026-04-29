package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class VenueCoordinatesBackfill {

    private static final Logger log = LoggerFactory.getLogger(VenueCoordinatesBackfill.class);

    @Bean
    CommandLineRunner backfillVenueCoordinates(VenueService venueService) {
        return args -> {
            int updated = 0;
            for (Venue venue : venueService.findAll()) {
                if (venue.getLatitude() != null && venue.getLongitude() != null) {
                    continue;
                }

                // Re-saving triggers best-effort coordinate enrichment in VenueServiceImpl.
                venueService.save(venue);
                updated++;
            }

            if (updated > 0) {
                log.info("Backfilled coordinates for {} venue(s)", updated);
            }
        };
    }
}
