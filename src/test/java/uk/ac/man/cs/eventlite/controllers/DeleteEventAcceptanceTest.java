package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.dao.EventRepository;
import uk.ac.man.cs.eventlite.entities.Event;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeleteEventAcceptanceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventRepository eventRepository;

    private Event getDeleteTestEvent() {
        Iterable<Event> events = eventRepository.findAll();
        Iterator<Event> it = events.iterator();

        while (it.hasNext()) {
            Event e = it.next();
            if ("Delete Test Event".equals(e.getName())) {
                return e;
            }
        }

        throw new IllegalStateException("Delete Test Event not found in test database");
    }

    @Test
    public void deleteIsNotAvailableFromEventsListPage() throws Exception {
        mvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("action=\"/my/events/"))));
    }

    @Test
    public void deleteIsNotAvailableFromMyEventsListPage() throws Exception {
        mvc.perform(get("/my/events")
                .with(user("Rob").roles("ADMINISTRATOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("action=\"/my/events/"))));
    }

    @Test
    public void deleteIsAvailableFromDetailedEventPage() throws Exception {
        Event event = getDeleteTestEvent();

        mvc.perform(get("/events/" + event.getId())
                .with(user("Rob").roles("ADMINISTRATOR")))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getEvent"))
            .andExpect(content().string(containsString("Delete")));
    }

    @Test
    public void deletingFromDetailedPageRemovesTheWholeEvent() throws Exception {
        Event event = getDeleteTestEvent();
        long id = event.getId();

        mvc.perform(delete("/my/events/" + id)
                .with(user("Rob").roles("ADMINISTRATOR"))
                .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:/my/events"));

        assertFalse(eventRepository.findById(id).isPresent());
    }
}