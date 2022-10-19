package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;
import com.dogginer.dog.repository.IClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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
class ClientServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IClientRepository clientRepository;

    @Autowired
    private IClientService clientService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private List<Client> clientList = new ArrayList<>();

    @BeforeEach
    void beforeEach() {
        for (int i = 0; i < 3; i ++) {
            Client client = new Client();
            client.setPassword("pwd" + i);
            client.setUsername("client" + i);
            client.setEmail("email" + i);
            client.setClientId(i + 1);
            this.clientList.add(client);
        }

    }

    @AfterEach
    void afterEach() {
        this.clientList = new ArrayList<>();
    }

    @Test
    void findAll() {
        when(clientRepository.findAll()).thenReturn(clientList);

        List<Client> clients = clientService.findAll();

        assertNotNull(clients, "client list returned");
        assertEquals(3, clients.size(), "three clients returned");
        assertEquals(clientList.get(0), clients.get(0), "first client is correct");
        assertEquals(clientList.get(1), clients.get(1), "second client is correct");
        assertEquals(clientList.get(2), clients.get(2), "third client is correct");

    }

    @Test
    void findById() {
        when(clientRepository.findById(0)).thenReturn(Optional.empty());
        when(clientRepository.findById(1)).thenReturn(Optional.of(clientList.get(0)));

        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(0),
                "inexistent client throws exception");

        Client client1 = clientService.findById(1);
        assertNotNull(client1, "a client object is returned");
        assertNotNull(client1.getClientId(), "a clientId is returned");
        assertEquals(1, client1.getClientId(), "correct clientId returned");
        assertEquals(clientList.get(0).getEmail(), client1.getEmail(), "correct email is returned");
        assertEquals(clientList.get(0).getUsername(), client1.getUsername(), "correct username is returned");
        assertEquals(clientList.get(0).getPassword(), client1.getPassword(), "correct username is returned");
    }

    @Test
    void addClient() {
        when(clientRepository.save(any())).then(mockRepositorySave());

        Client newClient = clientList.get(0);
        newClient.setClientId(null);
        Client savedClient = clientService.addClient(newClient);
        assertNotNull(savedClient, "a client object is returned");
        assertNotNull(savedClient.getClientId(), "a clientId is returned");
        assertEquals(7, savedClient.getClientId(), "correct clientId returned");
        assertEquals(newClient.getEmail(), savedClient.getEmail(), "correct email is returned");
        assertEquals(newClient.getUsername(), savedClient.getUsername(), "correct username is returned");
        assertTrue(passwordEncoder.matches("pwd0", savedClient.getPassword()),
                "password has been encoded correctly");

        newClient.setUsername("repeated");
        assertThrows(BadRequestException.class, () -> clientService.addClient(newClient), "wrong data throws exception");
    }

    @Test
    void deleteById() {
        when(clientRepository.findById(0)).thenReturn(Optional.empty());
        when(clientRepository.findById(1)).thenReturn(Optional.of(clientList.get(0)));
        doNothing().when(clientRepository).deleteById(ArgumentMatchers.anyInt());

        Client deletedClient = clientService.deleteById(1);
        assertNotNull(deletedClient, "a client object is returned");
        assertNotNull(deletedClient.getClientId(), "a clientId is returned");
        assertEquals(1, deletedClient.getClientId(), "correct clientId returned");
        assertEquals(clientList.get(0).getEmail(), deletedClient.getEmail(), "correct email is returned");
        assertEquals(clientList.get(0).getUsername(), deletedClient.getUsername(), "correct username is returned");
        assertEquals(clientList.get(0).getPassword(), deletedClient.getPassword(), "correct pwd is returned");

        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteById(0));
    }

    @Test
    void updateClient() {
        when(clientRepository.save(any())).then(mockRepositorySave());
        when(clientRepository.findById(0)).thenReturn(Optional.empty());
        when(clientRepository.findById(1)).thenReturn(Optional.of(clientList.get(0)));

        Client newClientInfo = clientList.get(2);
        Client updatedClient = clientService.updateClient(1, newClientInfo);

        assertNotNull(updatedClient.getClientId(), "a clientId is returned");
        assertEquals(1, updatedClient.getClientId(), "correct clientId returned");
        assertEquals(clientList.get(2).getEmail(), updatedClient.getEmail(), "correct email is returned");
        assertEquals(clientList.get(2).getUsername(), updatedClient.getUsername(), "correct username is returned");
        assertEquals(clientList.get(2).getPassword(), updatedClient.getPassword(), "correct pwd is returned");

        assertThrows(ResourceNotFoundException.class, () -> clientService.updateClient(0, newClientInfo));

        newClientInfo.setUsername("repeated");
        assertThrows(BadRequestException.class, () -> clientService.updateClient(1, newClientInfo));

    }

    @Test
    void partiallyUpdateClient() {
        when(clientRepository.save(any())).then(mockRepositorySave());
        when(clientRepository.findById(anyInt())).then(invocation -> {
            int clientId = invocation.getArgument(0);
            if (clientId < 1 || clientId > 3) throw new ResourceNotFoundException("cleintId:" + clientId);
            return Optional.of(clientList.get(clientId - 1));
        });
        when(clientRepository.findById(1)).thenReturn(Optional.of(clientList.get(0)));

        Client username = new Client();
        username.setUsername("new username");
        Client updatedClient = clientService.partiallyUpdateClient(1, username);

        assertNotNull(updatedClient.getClientId(), "a clientId is returned");
        assertEquals(1, updatedClient.getClientId(), "correct clientId returned");
        assertEquals(clientList.get(0).getEmail(), updatedClient.getEmail(), "correct email is returned");
        assertEquals("new username", updatedClient.getUsername(), "correct username is returned");
        assertEquals(clientList.get(0).getPassword(), updatedClient.getPassword(), "correct username is returned");

        assertThrows(ResourceNotFoundException.class, () -> clientService.partiallyUpdateClient(0, username));

        username.setUsername("repeated");
        assertThrows(BadRequestException.class, () -> clientService.partiallyUpdateClient(1, username));

        Client email = new Client();
        email.setEmail("new email");
        updatedClient = clientService.partiallyUpdateClient(2, email);
        assertEquals("new email", updatedClient.getEmail(), "correct email is returned");
        assertEquals(clientList.get(1).getUsername(), updatedClient.getUsername(), "correct username is returned");
        assertEquals(clientList.get(1).getPassword(), updatedClient.getPassword(), "correct pwd is returned");

        Client password = new Client();
        password.setPassword("new");
        updatedClient = clientService.partiallyUpdateClient(3, password);
        assertEquals(clientList.get(2).getEmail(), updatedClient.getEmail(), "correct email is returned");
        assertEquals(clientList.get(2).getUsername(), updatedClient.getUsername(), "correct username is returned");
        assertTrue(passwordEncoder.matches("new", updatedClient.getPassword()), "correct password is returned");
    }



    private Answer<Object> mockRepositorySave() {
        return invocation -> {
            Client client = invocation.getArgument(0);
            if (client.getUsername().equals("repeated")) throw new Exception();
            if (client.getClientId() == 0) client.setClientId(7);
            return client;
        };
    }
}