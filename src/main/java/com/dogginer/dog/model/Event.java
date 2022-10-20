package com.dogginer.dog.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="events")
@NamedQuery(name="Event.findAll", query="SELECT e FROM Event e")
public  @Data class Event extends RepresentationModel<Event> implements Serializable{
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_IMAGE_URL = "https://picsum.photos/id/237/300";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;

    private String title;
    private String description;
    private LocalDateTime date;
    private String imageUrl = DEFAULT_IMAGE_URL;
    private Double price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Event event = (Event) o;
        return getEventId().equals(event.getEventId()) &&
                getTitle().equals(event.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getEventId(), getTitle());
    }
}
