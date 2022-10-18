package com.dogginer.dog.controller;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Event;
import com.dogginer.dog.service.IEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = EventController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private IEventService eventService;

    private List<Event> eventList = new ArrayList<>();

    @BeforeEach
    void generateEventList() {
        for (int i = 0; i < 3; i ++) {
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

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void emptyEventList() {
        this.eventList = new ArrayList<>();
    }

    @Test
    void getAllEvents() throws Exception {
        when(eventService.findAll()).thenReturn(eventList);

        mockMvc.perform(get("/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.eventList", hasSize(3)))
                .andExpect(jsonPath("$._embedded.eventList[0].eventId", is(1)))
                .andExpect(jsonPath("$._embedded.eventList[0].title", is("eventTitle1")))
                .andExpect(jsonPath("$._embedded.eventList[0].description", is("eventDescription1")))
                .andExpect(jsonPath("$._embedded.eventList[0].imageUrl", is("https://picsum.photos/id/1000/300")))
                .andExpect(jsonPath("$._embedded.eventList[0].price", is(0.01)))
                .andExpect(jsonPath("$._embedded.eventList[0]._links").exists())
                .andExpect(jsonPath("$._embedded.eventList[0]._links.self.href", is("http://localhost/v1/events/1")))
                .andExpect(jsonPath("$._embedded.eventList[1].eventId", is(2)))
                .andExpect(jsonPath("$._embedded.eventList[2].eventId", is(3)));
    }

    @Test
    void getEvent() throws Exception {
        when(eventService.findById(ArgumentMatchers.anyInt())).then(invocation -> {
            int eventId = invocation.getArgument(0);
            if (eventId > 3 || eventId < 1)
                throw new ResourceNotFoundException("eventId:" + invocation.getArgument(0));
            return eventList.get(eventId - 1);
        });

        mockMvc.perform(get("/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.title", is("eventTitle1")))
                .andExpect(jsonPath("$.description", is("eventDescription1")))
                .andExpect(jsonPath("$.imageUrl", is("https://picsum.photos/id/1000/300")))
                .andExpect(jsonPath("$.price", is(0.01)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")));

        mockMvc.perform(get("/v1/events/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(2)))
                .andExpect(jsonPath("$.title", is("eventTitle2")))
                .andExpect(jsonPath("$.description", is("eventDescription2")))
                .andExpect(jsonPath("$.imageUrl", is("https://picsum.photos/id/1003/300")))
                .andExpect(jsonPath("$.price", is(0.02)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")));

        mockMvc.perform(get("/v1/events/73"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEvent() throws Exception {
        when(eventService.addEvent(ArgumentMatchers.any(Event.class))).then(invocation -> {
            Event event = invocation.getArgument(0);
            if (event.getEventId() != null && event.getEventId() == -1) throw new BadRequestException("Bad request");
            event.setEventId(1);
            return event;
        });

        Event newEvent = new Event();
        newEvent.setPrice(10.0);
        newEvent.setDescription("New event description");
        newEvent.setTitle("New event");
        newEvent.setImageUrl("https://picsum.photos/id/237/300");

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.title", is("New event")))
                .andExpect(jsonPath("$.description", is("New event description")))
                .andExpect(jsonPath("$.imageUrl", is("https://picsum.photos/id/237/300")))
                .andExpect(jsonPath("$.price", is(10.00)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/events/1")));

        newEvent.setEventId(-1);
        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent() throws Exception {

        when(eventService.updateEvent(ArgumentMatchers.anyInt(), ArgumentMatchers.any(Event.class)))
                .then(invocation -> {
                    int eventId = invocation.getArgument(0);
                    if (eventId > 3) throw new ResourceNotFoundException("eventId:" + eventId);
                    if (eventId == 0) throw new BadRequestException("Bad request");
                    return invocation.getArgument(1);
                });

        Event updatedEvent = eventList.get(0);
        updatedEvent.setImageUrl("this is a new imageUrl");
        updatedEvent.setTitle("updated title");
        updatedEvent.setDate(null);

        mockMvc.perform(put("/v1/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.title", is("updated title")))
                .andExpect(jsonPath("$.description", is("eventDescription1")))
                .andExpect(jsonPath("$.imageUrl", is("this is a new imageUrl")))
                .andExpect(jsonPath("$.price", is(0.01)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/events/1")));

        mockMvc.perform(put("/v1/events/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/v1/events/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void partiallyUpdateEvent() throws Exception {
        when(eventService.partiallyUpdateEvent(ArgumentMatchers.anyInt(), ArgumentMatchers.any(Event.class)))
                .then(invocation -> {
                    int eventId = invocation.getArgument(0);
                    if (eventId > 3) throw new ResourceNotFoundException("eventId:" + eventId);
                    if (eventId == 0) throw new BadRequestException("Bad request");
                    Event update = invocation.getArgument(1);

                    Event updatedEvent = eventList.get(0);
                    updatedEvent.setImageUrl(update.getImageUrl());
                    return updatedEvent;
                });

        Event eventUpdate = new Event();
        eventUpdate.setImageUrl("new imageUrl");
        mockMvc.perform(patch("/v1/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.title", is("eventTitle1")))
                .andExpect(jsonPath("$.description", is("eventDescription1")))
                .andExpect(jsonPath("$.imageUrl", is("new imageUrl")))
                .andExpect(jsonPath("$.price", is(0.01)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/events/1")));

        mockMvc.perform(patch("/v1/events/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventUpdate)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/v1/events/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEvent() throws Exception {

        when(eventService.deleteById(1)).thenReturn(eventList.get(0));
        when(eventService.deleteById(0)).thenThrow(new ResourceNotFoundException("eventId:0"));

        mockMvc.perform(delete("/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.title", is("eventTitle1")))
                .andExpect(jsonPath("$.description", is("eventDescription1")))
                .andExpect(jsonPath("$.imageUrl", is("https://picsum.photos/id/1000/300")))
                .andExpect(jsonPath("$.price", is(0.01)))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.all-events.href", is("http://localhost/v1/events/")));

        mockMvc.perform(delete("/v1/events/0"))
                .andExpect(status().isNotFound());
    }
}