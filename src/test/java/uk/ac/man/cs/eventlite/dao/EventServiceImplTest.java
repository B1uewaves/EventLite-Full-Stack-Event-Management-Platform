package uk.ac.man.cs.eventlite.dao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.man.cs.eventlite.entities.Event;

class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    private EventServiceImpl eventService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventServiceImpl(eventRepository);
    }

    @Test
    void countShouldReturnRepositoryValue() {
        when(eventRepository.count()).thenReturn(3L);
        long result = eventService.count();
        assertEquals(3L, result);
        verify(eventRepository).count();
    }

    @Test
    void findByIdShouldCallRepository() {
        long id = 21L;
        eventService.findById(id);
        verify(eventRepository).findById(id);
    }

    @Test
    void deleteByIdShouldCallRepository() {
        long id = 25L;
        eventService.deleteById(id);
        verify(eventRepository).deleteById(id);
    }

    @Test
    void saveShouldDelegateToRepository() {
        Event event = new Event();
        eventService.save(event);
        verify(eventRepository).save(event);
    }

    @Test
    void saveForUserShouldSetUsernameBeforeSaving() {
        Event event = new Event();
        eventService.saveForUser(event, "Alice");
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        assertEquals("Alice", captor.getValue().getOwnerUsername());
    }

    @Test
    void findAllForUserShouldCallRepository() {
        String username = "Alice";

        eventService.findAllForUser(username);

        verify(eventRepository)
            .findByOwnerUsernameOrderByDateAscTimeAsc(username);
    }

    @Test
    void searchShouldReturnAllWhenNameIsNull() {
        eventService.search(null);

        verify(eventRepository).findAll();
    }

    @Test
    void searchShouldReturnAllWhenNameIsBlank() {
        eventService.search("   ");

        verify(eventRepository).findAll();
    }

    @Test
    void findNext3EventsShouldCallRepository() {
        long venueId = 2L;
        LocalDate date = LocalDate.now();
        eventService.findNext3EventsByVenueId(venueId, date);

        verify(eventRepository)
            .findTop3ByVenueIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(venueId, date);
    }

    @Test
    void findPastEventsShouldCallRepository() {
        LocalDate date = LocalDate.now();
        eventService.findPastEvents(date);

        verify(eventRepository).findByDateBefore(date);
    }

    @Test
    void findUpcomingEventsShouldCallRepository() {
        LocalDate date = LocalDate.now();
        eventService.findUpcomingEvents(date);

        verify(eventRepository).findByDateGreaterThanEqual(date);
    }

}
