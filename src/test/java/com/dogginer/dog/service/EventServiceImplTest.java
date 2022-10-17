package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties")
@SpringBootTest
public class EventServiceImplTest {

    private Event testEvent = null;

    //constants
    private final String TEST_EVENT_TITLE = "Test event";
    private final String TEST_EVENT_DESCRIPTION = "This is a testEvent";
    private final String TEST_EVENT_IMG_URL = "www.testImage.html";
    private final LocalDateTime TEST_EVENT_DATETIME = LocalDateTime.of(2022,10,23,0,0);
    private final double TEST_EVENT_PRICE = 10.0;

    @Autowired
    IEventService eventService;

    @BeforeEach
    public void beforeEach() {
        this.createTestEvent();
    }


    @AfterEach
    public void afterEach() {
        this.deleteTestEvent();
    }

    private void createTestEvent() {
        Event event = new Event();
        event.setTitle(this.TEST_EVENT_TITLE);
        event.setDescription(this.TEST_EVENT_DESCRIPTION);
        event.setImageUrl(this.TEST_EVENT_IMG_URL);
        event.setDate(this.TEST_EVENT_DATETIME);
        event.setPrice(this.TEST_EVENT_PRICE);
        this.testEvent = eventService.addEvent(event);
    }

    private void deleteTestEvent() {
        if (this.testEvent != null)
            eventService.deleteById(this.testEvent.getEventId());

        this.testEvent = null;
    }

    @Test
    public void findById() {
        Event event = eventService.findById(this.testEvent.getEventId());

        assertNotNull(event, "event is retrieved");
        assertEquals(this.TEST_EVENT_TITLE, event.getTitle(), "title is correct");
        assertEquals(this.TEST_EVENT_DESCRIPTION, event.getDescription(), "description is correct");
        assertEquals(this.TEST_EVENT_PRICE, event.getPrice(), "price is correct");
        assertEquals(this.TEST_EVENT_DATETIME, event.getDate(), "date is correct");
        assertEquals(this.TEST_EVENT_IMG_URL, event.getImageUrl(), "imageUrl is correct");

        assertThrows(ResourceNotFoundException.class, () -> eventService.findById(1000),
                "inexistent id search throws exception");
    }

    @Test
    public void createEvent() {
        Event retrievedEvent = eventService.findById(this.testEvent.getEventId());

        assertNotEquals(this.testEvent.getEventId(), 0, "an id has been generated");
        assertEquals(retrievedEvent, this.testEvent, "saved id and retrieved id match");

    }

    @Test
    public void findAll() {
        Event[] sampleEvents = {new Event(), new Event(), new Event()};
        for (int i = 0; i < 3; i ++) {
            Event event = sampleEvents[i];
            event.setTitle("Event" + i);
            event.setDescription("Event " + i + "description");
            event.setPrice(i * 3.0);
            event.setDate(TEST_EVENT_DATETIME);
            event.setImageUrl("url" + i);
            sampleEvents[i] = eventService.addEvent(event);
        }

        List<Event> retrievedEvents = eventService.findAll();
        Event lastEvent = retrievedEvents.get(retrievedEvents.size() - 1);

        assertTrue(retrievedEvents.size() >= 3, "there are at least three entries");
        assertEquals("Event2", lastEvent.getTitle(), "last title is correct");
        assertEquals(6.0, lastEvent.getPrice(), "last price is correct");

        for (int i = 0; i < 3; i ++) {
            eventService.deleteById(sampleEvents[i].getEventId());
        }
    }

    @Test
    public void attemptWrongEventCreate() {

        int existingEventsAtStart = eventService.findAll().size();

        //bases event
        Event wrongEvent = new Event();
        wrongEvent.setTitle(this.TEST_EVENT_TITLE);
        wrongEvent.setDescription(this.TEST_EVENT_DESCRIPTION);
        wrongEvent.setImageUrl(this.TEST_EVENT_IMG_URL);
        wrongEvent.setPrice(this.TEST_EVENT_PRICE);
        wrongEvent.setDate(this.TEST_EVENT_DATETIME);

        //missing title
        wrongEvent.setTitle(null);
        assertThrows(BadRequestException.class, () -> eventService.addEvent(wrongEvent),
                "add event missing title throws exception");
        assertEquals(existingEventsAtStart, eventService.findAll().size(), "event has not been create");
        wrongEvent.setTitle(this.TEST_EVENT_TITLE);

        //missing description
        wrongEvent.setDescription(null);
        assertThrows(BadRequestException.class, () -> eventService.addEvent(wrongEvent),
                "add event missing description throws exception");
        assertEquals(existingEventsAtStart, eventService.findAll().size(), "event has not been created");
        wrongEvent.setDescription(TEST_EVENT_DESCRIPTION);

        //missing date
        wrongEvent.setDate(null);
        assertThrows(BadRequestException.class, () -> eventService.addEvent(wrongEvent),
                "add event missing date throws exception");
        assertEquals(existingEventsAtStart, eventService.findAll().size(), "event has not been created");
        wrongEvent.setDate(TEST_EVENT_DATETIME);

        //missing price
        wrongEvent.setPrice(null);
        assertThrows(BadRequestException.class, () -> eventService.addEvent(wrongEvent),
                "add event missing price throws exception");
        assertEquals(existingEventsAtStart, eventService.findAll().size(), "event has not been created");
        wrongEvent.setPrice(TEST_EVENT_PRICE);

        //negative price
        wrongEvent.setPrice(-10.0);
        assertThrows(BadRequestException.class, () -> eventService.addEvent(wrongEvent),
                "add event negative price throws exception");
        assertEquals(existingEventsAtStart, eventService.findAll().size(), "event has not been created");
        wrongEvent.setPrice(TEST_EVENT_PRICE);
    }

    @Test
    public void updateEvent() {
        Event eventNewInfo = new Event();
        eventNewInfo.setEventId(1000);
        eventNewInfo.setPrice(20.0);
        eventNewInfo.setImageUrl("new image url");
        eventNewInfo.setDate(LocalDateTime.of(2023, 10,23,0,0));
        eventNewInfo.setDescription("new description");
        eventNewInfo.setTitle("new title");

        Event updatedEvent = eventService.updateEvent(this.testEvent.getEventId(), eventNewInfo);

        //assert returned data is correct
        assertNotNull(updatedEvent, "updated event not null");
        assertEquals(testEvent.getEventId(), updatedEvent.getEventId(), "returned eventId is correct");
        assertEquals(eventNewInfo.getPrice(), updatedEvent.getPrice(), "returned price is correct");
        assertEquals(eventNewInfo.getDescription(), updatedEvent.getDescription(),
                "returned description is correct");
        assertEquals(eventNewInfo.getTitle(), updatedEvent.getTitle(), "returned title is correct");
        assertEquals(eventNewInfo.getImageUrl(), updatedEvent.getImageUrl(), "returned imageUrl is correct");
        assertEquals(eventNewInfo.getDate(), updatedEvent.getDate(), "returned date is correct");

        //assert data was correctly saved to DB
        Event retrievedEvent = eventService.findById(updatedEvent.getEventId());
        assertEquals(testEvent.getEventId(), retrievedEvent.getEventId(), "saved eventId is correct");
        assertEquals(eventNewInfo.getPrice(), retrievedEvent.getPrice(), "saved price is correct");
        assertEquals(eventNewInfo.getDescription(), retrievedEvent.getDescription(),
                "saved description is correct");
        assertEquals(eventNewInfo.getTitle(), retrievedEvent.getTitle(), "saved title is correct");
        assertEquals(eventNewInfo.getImageUrl(), retrievedEvent.getImageUrl(), "saved imageUrl is correct");
        assertEquals(eventNewInfo.getDate(), retrievedEvent.getDate(), "saved date is correct");
    }

    @Test
    public void partiallyUpdateEvent() {
        Event eventNewInfo = new Event();
        eventNewInfo.setEventId(1000);
        eventNewInfo.setPrice(20.0);
        eventNewInfo.setImageUrl(null);
        eventNewInfo.setTitle("new title");

        Event updatedEvent = eventService.partiallyUpdateEvent(this.testEvent.getEventId(), eventNewInfo);

        //assert returned data is correct
        assertNotNull(updatedEvent, "updated event not null");
        assertEquals(testEvent.getEventId(), updatedEvent.getEventId(), "returned eventId is correct");
        assertEquals(eventNewInfo.getPrice(), updatedEvent.getPrice(), "returned price is correct");
        assertEquals(testEvent.getDescription(), updatedEvent.getDescription(),
                "returned description is correct");
        assertEquals(eventNewInfo.getTitle(), updatedEvent.getTitle(), "returned title is correct");
        assertEquals(testEvent.getImageUrl(), updatedEvent.getImageUrl(), "returned imageUrl is correct");
        assertEquals(testEvent.getDate(), updatedEvent.getDate(), "returned date is correct");

        //assert data was correctly saved to DB
        Event retrievedEvent = eventService.findById(updatedEvent.getEventId());
        assertEquals(testEvent.getEventId(), retrievedEvent.getEventId(), "saved eventId is correct");
        assertEquals(eventNewInfo.getPrice(), retrievedEvent.getPrice(), "saved price is correct");
        assertEquals(testEvent.getDescription(), retrievedEvent.getDescription(),
                "saved description is correct");
        assertEquals(eventNewInfo.getTitle(), retrievedEvent.getTitle(), "saved title is correct");
        assertEquals(testEvent.getImageUrl(), retrievedEvent.getImageUrl(), "saved imageUrl is correct");
        assertEquals(testEvent.getDate(), retrievedEvent.getDate(), "saved date is correct");
    }

    @Test
    public void deleteEvent() {
        Event deletedEvent = eventService.deleteById(this.testEvent.getEventId());
        //assert returned data is correct
        assertNotNull(deletedEvent, "deletedEvent is returned");

        //assert returned data is correct
        assertNotNull(deletedEvent, "updated event not null");
        assertEquals(testEvent.getEventId(), deletedEvent.getEventId(), "returned eventId is correct");
        assertEquals(testEvent.getPrice(), deletedEvent.getPrice(), "returned price is correct");
        assertEquals(testEvent.getDescription(), deletedEvent.getDescription(),
                "returned description is correct");
        assertEquals(testEvent.getTitle(), deletedEvent.getTitle(), "returned title is correct");
        assertEquals(testEvent.getImageUrl(), deletedEvent.getImageUrl(), "returned imageUrl is correct");
        assertEquals(testEvent.getDate(), deletedEvent.getDate(), "returned date is correct");

        //confirm the event has been deleted from the db
        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteById(deletedEvent.getEventId()),
                "deleted event not found");

        this.testEvent = null;

    }
}
