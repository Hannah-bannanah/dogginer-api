package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.DuplicateEntryException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Event;
import com.dogginer.dog.repository.IEventRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements IEventService {

    private IEventRepository eventRepository;

    @Autowired public EventServiceImpl(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves all events
     * @return the list of all events
     */
    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    /**
     * Searches for an event by id
     * @param eventId
     * @return the event if found, null otherwise
     */
    @Override
    public Event findById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("eventId:" + eventId));
    }

    /**
     * Creates a new event
     * @param event an object with all event data
     * @return the created event object
     */
    @Override
    public Event addEvent(Event event) {
        event.setEventId(0);
        Event savedEvent = eventRepository.save(event);
        return savedEvent;
    }

    /**
     * Replaces an existing event with a new one
     * @param eventId the id of the event to be replaced
     * @param event the new event object
     */
    @Override
    public Event updateEvent(int eventId, Event event) {
        Event existingEvent =  eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("eventId:" + eventId));
        event.setEventId(existingEvent.getEventId());
        return eventRepository.save(event);
    }

    /**
     * Makes partial updates to an existing event
     * @param eventId the id of the event to be updated
     * @param eventUpdates an object with the fields to be modified
     * @return the updated event object
     */
    @Override
    public Event partiallyUpdateEvent(int eventId, Event eventUpdates) {
        Event updatedEvent =  eventRepository.findById(eventId)
                .map(existingEvent -> copyNonNullFields(eventUpdates, existingEvent))
                .orElseThrow(() -> new ResourceNotFoundException("eventId:" + eventId));
        return eventRepository.save(updatedEvent);
    }

    @Override
    public Event deleteById(int eventId) {
        Event deletedEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("eventId:" + eventId));
        eventRepository.deleteById(eventId);
        return deletedEvent;
    }


    private Event copyNonNullFields(Event origin, Event destination) {
        if (origin.getDate() != null) destination.setDate(origin.getDate());
        if (StringUtils.isNotEmpty(origin.getDescription()))
            destination.setDescription(origin.getDescription());
        if (StringUtils.isNotEmpty(origin.getImageUrl()))
            destination.setImageUrl(origin.getImageUrl());
        if (origin.getPrice() != null) destination.setPrice(origin.getPrice());
        if (StringUtils.isNotEmpty(origin.getTitle())) destination.setTitle(origin.getTitle());
        return destination;
    }

}
