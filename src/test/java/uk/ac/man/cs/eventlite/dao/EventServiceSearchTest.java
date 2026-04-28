package uk.ac.man.cs.eventlite.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(MockitoExtension.class)
public class EventServiceSearchTest {

	@Mock
	private EventRepository eventRepository;

	@InjectMocks
	private EventServiceImpl eventService;

	private Venue venueA;
	private Venue venueB;

	@BeforeEach
	public void setup() {
		venueA = createVenue("Venue A");
		venueB = createVenue("Venue B");
	}

	@Test
	public void searchForAppleReturnsOnlyEventApple() {
		when(eventRepository.findAll()).thenReturn(sampleData());

		List<Event> results = toList(eventService.search("apple"));

		assertThat(results)
			.extracting(Event::getName)
			.containsExactly("Event Apple");
	}

	@Test
	public void upcomingEventsAreOrderedByDateThenName() {
		LocalDate today = LocalDate.of(2026, 1, 1);

		when(eventRepository.findByDateGreaterThanEqual(today)).thenReturn(List.of(
			createEvent("Event Beta", LocalDate.of(2026, 7, 11), LocalTime.of(10, 0), venueA),
			createEvent("Event Apple", LocalDate.of(2026, 7, 12), null, venueA),
			createEvent("Event Alpha", LocalDate.of(2026, 7, 11), LocalTime.of(12, 30), venueB)
		));

		List<Event> results = toList(eventService.findUpcomingEvents(today));

		assertThat(results)
			.extracting(Event::getName)
			.containsExactly(
				"Event Alpha",
				"Event Beta",
				"Event Apple"
			);
	}

	@Test
	public void pastEventsAreOrderedByDateDescendingThenName() {
		LocalDate today = LocalDate.of(2026, 7, 1);

		when(eventRepository.findByDateBefore(today)).thenReturn(List.of(
			createEvent("Event Previous", LocalDate.of(2026, 1, 11), LocalTime.of(18, 30), venueA),
			createEvent("Event Past", LocalDate.of(2026, 1, 10), LocalTime.of(17, 0), venueA),
			createEvent("Event Former", LocalDate.of(2026, 1, 11), LocalTime.of(11, 0), venueB)
		));

		List<Event> results = toList(eventService.findPastEvents(today));

		assertThat(results)
			.extracting(Event::getName)
			.containsExactly(
				"Event Former",
				"Event Previous",
				"Event Past"
			);
	}

	private List<Event> sampleData() {
		return List.of(
			createEvent("Event Alpha", LocalDate.of(2026, 7, 11), LocalTime.of(12, 30), venueB),
			createEvent("Event Beta", LocalDate.of(2026, 7, 11), LocalTime.of(10, 0), venueA),
			createEvent("Event Apple", LocalDate.of(2026, 7, 12), null, venueA),
			createEvent("Event Former", LocalDate.of(2026, 1, 11), LocalTime.of(11, 0), venueB),
			createEvent("Event Previous", LocalDate.of(2026, 1, 11), LocalTime.of(18, 30), venueA),
			createEvent("Event Past", LocalDate.of(2026, 1, 10), LocalTime.of(17, 0), venueA)
		);
	}

	private Venue createVenue(String name) {
		Venue venue = new Venue();
		venue.setName(name);
		venue.setCapacity(100);
		venue.setPostcode("M13 9PL");
		venue.setAddress("Oxford Road, Manchester");
		return venue;
	}

	private Event createEvent(String name, LocalDate date, LocalTime time, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDate(date);
		event.setTime(time);
		event.setVenue(venue);
		return event;
	}

	private List<Event> toList(Iterable<Event> events) {
		return StreamSupport.stream(events.spliterator(), false).toList();
	}
}