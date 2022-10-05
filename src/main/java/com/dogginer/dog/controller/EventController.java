package com.dogginer.dog.controller;

import com.dogginer.dog.model.Event;
import com.dogginer.dog.service.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    private IEventService eventService;
    private static Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired public EventController(IEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("")
    public List<Event> getAllEvents() {
        logger.debug("Received GET request at endpoint v1/events");
        return eventService.findAll();
    }

    @GetMapping("/{eventId}")
    public Event getEvent(@PathVariable int eventId) {
        logger.debug("Received GET request at endpoint v1/events/" + eventId);

        Event event = eventService.findById(eventId);

        event.add(linkTo(methodOn(this.getClass()).getAllEvents()).withRel("all-events"));
        return event;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@RequestBody Event event) {
        logger.debug("Received POST request at endpoint v1/events/");

        Event createdEvent = eventService.addEvent(event);

        createdEvent.add(linkTo(methodOn(this.getClass()).getAllEvents()).withRel("all-events"));
        createdEvent.add(linkTo(methodOn(this.getClass()).getEvent(createdEvent.getEventId())).withSelfRel());

        return createdEvent;
    }

    @PutMapping("/{eventId}")
    public Event updateEvent(@PathVariable int eventId, @RequestBody Event event) {
        logger.debug("Received PUT request at endpoint v1/events/" + eventId);

        Event updatedEvent = eventService.updateEvent(eventId, event);

        updatedEvent.add(linkTo(methodOn(this.getClass()).getAllEvents()).withRel("all-events"));
        updatedEvent.add(linkTo(methodOn(this.getClass()).getEvent(eventId)).withSelfRel());

        return updatedEvent;
    }

    @PatchMapping("/{eventId}")
    public Event partiallyUpdateEvent(@PathVariable int eventId, @RequestBody Event event) {
        logger.debug("Received PATCH request at endpoint v1/events/" + eventId);

        Event updatedEvent = eventService.partiallyUpdateEvent(eventId, event);

        updatedEvent.add(linkTo(methodOn(this.getClass()).getAllEvents()).withRel("all-events"));
        updatedEvent.add(linkTo(methodOn(this.getClass()).getEvent(eventId)).withSelfRel());

        return updatedEvent;
    }

    @DeleteMapping("/{eventId}")
    public Event delete(@PathVariable int eventId) {
        logger.debug("Received DELETE request at endpoint v1/events/" + eventId);

        Event deletedEvent = eventService.deleteById(eventId);

        deletedEvent.add(linkTo(methodOn(this.getClass()).getAllEvents()).withRel("all-events"));

        return deletedEvent;
    }
}
