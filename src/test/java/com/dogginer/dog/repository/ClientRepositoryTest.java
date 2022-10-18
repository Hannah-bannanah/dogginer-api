package com.dogginer.dog.repository;

import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ClientRepositoryTest {

    @Autowired private IClientRepository clientRepository;

//    @Autowired
//    BCryptPasswordEncoder passwordEncoder;


    @Test
    void findAll() {
        List<Client> clients = clientRepository.findAll();
        assertNotNull(clients, "a list of clients is returned");
        assertFalse(clients.isEmpty(), "the list of clients is not empty");
        assertEquals(3, clients.size(), "three clients exist");
    }

    @Test
    void findById_ok() {
        Optional<Client> client = clientRepository.findById(1);
        assertTrue(client.isPresent(), "client exists");
        assertEquals(client.get().getUsername(), "testClient1", "correct username");
        assertEquals(client.get().getEmail(), "testClient1@email.com", "correct email");
    }

    @Test
    void findById_ko() {
        Optional<Client> client = clientRepository.findById(0);
        assertFalse(client.isPresent(), "client doesn't exist");
    }
}
