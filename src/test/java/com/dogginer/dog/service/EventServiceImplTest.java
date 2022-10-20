package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Event;
import com.dogginer.dog.repository.IEventRepository;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class EventServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEventRepository eventRepository;

    @Autowired
    private IEventService eventService;

    private List<Event> eventList = new ArrayList<>();


    //constants
    private final String TEST_EVENT_TITLE = "Test event";
    private final String TEST_EVENT_DESCRIPTION = "This is a testEvent";
    private final String TEST_EVENT_IMG_URL = "www.testImage.html";
    private final LocalDateTime TEST_EVENT_DATETIME = LocalDateTime.of(2022,10,23,0,0);
    private final double TEST_EVENT_PRICE = 10.0;

    @BeforeEach
    void createEventList() {
        for (int i = 0; i < 4; i++) {
            Event event = new Event();
            event.setDate(LocalDateTime.now());
            event.setEventId(i + 1);
            event.setTitle("eventTitle" + (i + 1));
            event.setDescription("eventDescription" + (i + 1));
            event.setPrice(0.01 * (i + 1));
            int imageId = 1000 + 3*i;
            event.setImageUrl("https://picsum.photos/id/" + imageId + "/300");
            this.eventList.add(event);
        }
    }

    @AfterEach
    void deleteEventList() {
        eventList = new ArrayList<>();
    }

    @Test
    void findAll() {
        when(eventRepository.findAll()).thenReturn(eventList);

        List<Event> events = eventService.findAll();

        assertNotNull(events, "event list returned");
        assertEquals(4, events.size(), "four events are returned");
        assertEquals(eventList.get(0), events.get(0), "first event returned correctly");
        assertEquals(eventList.get(1), events.get(1), "second event returned correctly");
        assertEquals(eventList.get(2), events.get(2), "third event returned correctly");
        assertEquals(eventList.get(3), events.get(3), "forth event returned correctly");
        assertEquals("eventTitle1", events.get(0).getTitle(), "title returned correctly");
        assertEquals("eventDescription1", events.get(0).getDescription(), "description returned correctly");
        assertEquals("https://picsum.photos/id/1000/300", events.get(0).getImageUrl(), "imageUrl returned correctly");
        assertEquals(0.01, events.get(0).getPrice(), "price returned correctly");
    }

    @Test
    void findById() {
        when(eventRepository.findById(0)).thenReturn(Optional.empty());
        when(eventRepository.findById(1)).thenReturn(Optional.of(eventList.get(0)));

        assertThrows(ResourceNotFoundException.class, () -> eventService.findById(0));

        Event event1 = eventService.findById(1);
        assertNotNull(event1, "event object is returned");
        assertEquals(1, event1.getEventId(), "eventId is correct");
        assertEquals("eventTitle1", event1.getTitle(), "title returned correctly");
        assertEquals("eventDescription1", event1.getDescription(), "description returned correctly");
        assertEquals("https://picsum.photos/id/1000/300", event1.getImageUrl(), "imageUrl returned correctly");
        assertEquals(0.01, event1.getPrice(), "price returned correctly");
    }

    @Test
    void addEvent() {
        when(eventRepository.save(any())).then(mockRepositorySave());

        Event newEvent = this.createTestEvent();

        Event savedEvent = eventService.addEvent(newEvent);
        assertNotNull(savedEvent, "event object is returned");
        assertEquals(7, savedEvent.getEventId(), "eventId is correct");
        assertEquals(this.TEST_EVENT_TITLE, savedEvent.getTitle(), "title returned correctly");
        assertEquals(this.TEST_EVENT_DESCRIPTION, savedEvent.getDescription(), "description returned correctly");
        assertEquals(this.TEST_EVENT_IMG_URL, savedEvent.getImageUrl(), "imageUrl returned correctly");
        assertEquals(this.TEST_EVENT_PRICE, savedEvent.getPrice(), "price returned correctly");

        newEvent.setTitle("repeated");
        assertThrows(BadRequestException.class, () -> eventService.addEvent(newEvent));
    }

    @Test
    void updateEvent() {
        when(eventRepository.save(any())).then(mockRepositorySave());
        when(eventRepository.findById(0)).thenReturn(Optional.empty());
        when(eventRepository.findById(1)).thenReturn(Optional.of(eventList.get(0)));

        Event newEventInfo = this.createTestEvent();
        Event updatedEvent = eventService.updateEvent(1, newEventInfo);

        assertNotNull(updatedEvent, "an event object is returned");
        assertEquals(1, updatedEvent.getEventId(), "eventId is correct");
        assertEquals(this.TEST_EVENT_TITLE, updatedEvent.getTitle(), "title returned correctly");
        assertEquals(this.TEST_EVENT_DESCRIPTION, updatedEvent.getDescription(), "description returned correctly");
        assertEquals(this.TEST_EVENT_IMG_URL, updatedEvent.getImageUrl(), "imageUrl returned correctly");
        assertEquals(this.TEST_EVENT_PRICE, updatedEvent.getPrice(), "price returned correctly");

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(0, newEventInfo));

        newEventInfo.setTitle("repeated");
        assertThrows(BadRequestException.class, () -> eventService.updateEvent(1, newEventInfo));
    }

    @Test
    void partiallyUpdateEvent() {
        when(eventRepository.save(any())).then(mockRepositorySave());
        when(eventRepository.findById(anyInt())).then(invocation -> {
            int eventId = invocation.getArgument(0);
            if (eventId < 1 || eventId > 4) throw new ResourceNotFoundException("eventId:" + eventId);
            return Optional.of(eventList.get(eventId - 1));
        });


        Event title = new Event();
        title.setTitle("new title");

        Event description = new Event();
        description.setDescription("new description");

        Event imageUrl = new Event();
        imageUrl.setImageUrl("www.imageUrl.new");

        Event price = new Event();
        price.setPrice(100.0);

        Event updatedEvent = eventService.partiallyUpdateEvent(1, title);
        assertNotNull(updatedEvent, "an event object is returned");
        assertEquals(1, updatedEvent.getEventId(), "eventId is correct");
        assertEquals("new title", updatedEvent.getTitle(), "title has been updated");
        assertEquals("eventDescription1", updatedEvent.getDescription(), "description remains the same");
        assertEquals("https://picsum.photos/id/1000/300", updatedEvent.getImageUrl(), "imageUrl remains the same");
        assertEquals(0.01, updatedEvent.getPrice(), "price remains the same");

        updatedEvent = eventService.partiallyUpdateEvent(2, description);
        assertEquals(2, updatedEvent.getEventId(), "eventId is correct");
        assertEquals("eventTitle2", updatedEvent.getTitle(), "title remains the same");
        assertEquals("new description", updatedEvent.getDescription(), "description has been updated");
        assertEquals("https://picsum.photos/id/1003/300", updatedEvent.getImageUrl(), "imageUrl remains the same");
        assertEquals(0.02, updatedEvent.getPrice(), "price remains the same");

        updatedEvent = eventService.partiallyUpdateEvent(3, imageUrl);
        assertEquals(3, updatedEvent.getEventId(), "eventId is correct");
        assertEquals("eventTitle3", updatedEvent.getTitle(), "title remains the same");
        assertEquals("eventDescription3", updatedEvent.getDescription(), "description remains the same");
        assertEquals("www.imageUrl.new", updatedEvent.getImageUrl(), "imageUrl has been updated");
        assertEquals(0.03, updatedEvent.getPrice(), "price remains the same");

        updatedEvent = eventService.partiallyUpdateEvent(4, price);
        assertEquals(4, updatedEvent.getEventId(), "eventId is correct");
        assertEquals("eventTitle4", updatedEvent.getTitle(), "title remains the same");
        assertEquals("eventDescription4", updatedEvent.getDescription(), "description remains the same");
        assertEquals("https://picsum.photos/id/1009/300", updatedEvent.getImageUrl(), "imageUrl remains the same");
        assertEquals(100.0, updatedEvent.getPrice(), "price has been updated");

        title.setTitle("repeated");
        assertThrows(BadRequestException.class, () -> eventService.partiallyUpdateEvent(1, title));
        assertThrows(ResourceNotFoundException.class, () -> eventService.partiallyUpdateEvent(0, description));
    }

    @Test
    void deleteById() {
        when(eventRepository.findById(0)).thenReturn(Optional.empty());
        when(eventRepository.findById(1)).thenReturn(Optional.of(eventList.get(0)));
        doNothing().when(eventRepository).deleteById(anyInt());

        Event deletedEvent = eventService.deleteById(1);
        assertNotNull(deletedEvent, "event object is returned");
        assertEquals(1, deletedEvent.getEventId(), "eventId is correct");
        assertEquals("eventTitle1", deletedEvent.getTitle(), "title returned correctly");
        assertEquals("eventDescription1", deletedEvent.getDescription(), "description returned correctly");
        assertEquals("https://picsum.photos/id/1000/300", deletedEvent.getImageUrl(), "imageUrl returned correctly");
        assertEquals(0.01, deletedEvent.getPrice(), "price returned correctly");

        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteById(0));
    }

    private Answer<Object> mockRepositorySave() {
        return invocation -> {
            Event event = invocation.getArgument(0);
            if (event.getTitle().equals("repeated")) throw new Exception();
            if (event.getEventId() == 0) event.setEventId(7);
            return event;
        };
    }

    private Event createTestEvent() {
        Event event = new Event();
        event.setTitle(this.TEST_EVENT_TITLE);
        event.setDescription(this.TEST_EVENT_DESCRIPTION);
        event.setImageUrl(this.TEST_EVENT_IMG_URL);
        event.setDate(this.TEST_EVENT_DATETIME);
        event.setPrice(this.TEST_EVENT_PRICE);
        return event;
    }
}