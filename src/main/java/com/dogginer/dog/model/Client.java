package com.dogginer.dog.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name="clients")
@NamedQuery(name="Client.findAll", query="SELECT c FROM Client c")
public @Data class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clientId;

    private String email;
    private String username;
    private String password;

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
