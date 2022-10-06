package com.dogginer.dog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name="clients")
@NamedQuery(name="Client.findAll", query="SELECT c FROM Client c")
public @Data class Client extends RepresentationModel<Client> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clientId;

    private String email;
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    //uni-directional many-to-many association to Event
    @ManyToMany
    @JoinTable(
            name="attendees"
            , joinColumns={
                @JoinColumn(name="client_id")
            }
            , inverseJoinColumns={
                @JoinColumn(name="event_id")
            }
    )

    @JsonProperty(access = Access.READ_ONLY)
    private Set<Event> attendedEvents = new HashSet<>();

    public Client() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return getClientId() == client.getClientId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClientId());
    }

    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
