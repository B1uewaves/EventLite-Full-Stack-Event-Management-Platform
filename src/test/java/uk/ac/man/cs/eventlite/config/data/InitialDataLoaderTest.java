package uk.ac.man.cs.eventlite.config.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

class InitialDataLoaderTest {

    private final EventService eventService = mock(EventService.class);
    private final VenueService venueService = mock(VenueService.class);

    private final InitialDataLoader loader = new InitialDataLoader();

    @Test
    void initDatabaseDoesNothingWhenEventsAlreadyExist() throws Exception {
        when(eventService.count()).thenReturn(1L);

        CommandLineRunner runner = loader.initDatabase(eventService, venueService);
        runner.run();

        verify(eventService).count();
        verifyNoMoreInteractions(eventService);
        verifyNoInteractions(venueService);
    }

    @Test
    void initDatabaseCreatesInitialVenuesAndEventsWhenEmpty() throws Exception {
        when(eventService.count()).thenReturn(0L);

        CommandLineRunner runner = loader.initDatabase(eventService, venueService);
        runner.run();

        verify(eventService).count();

        verify(venueService, times(3)).save(any(Venue.class));
        verify(eventService, times(3)).save(any(Event.class));
    }
}