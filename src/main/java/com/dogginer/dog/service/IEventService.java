package com.dogginer.dog.service;

import com.dogginer.dog.model.Event;

import java.util.List;

public interface IEventService {
    List<Event> findAll();

    Event findById(int eventId);

    Event addEvent(Event event);

    Event updateEvent(int eventId, Event event);

    Event partiallyUpdateEvent(int eventId, Event event);

    Event deleteById(int eventId);
}
