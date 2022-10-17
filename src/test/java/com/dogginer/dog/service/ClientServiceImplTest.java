package com.dogginer.dog.service;

import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties")
@SpringBootTest
public class ClientServiceImplTest {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired IClientService clientService;

    private Client testClient = null;

    @BeforeEach
    public void beforeEach() {
        this.createTestClient();
    }

    @AfterEach
    public void afterEach() {
        this.deleteTestClient();
    }

    private void createTestClient() {
        Client client = new Client();
        client.setUsername("testClient");
        client.setEmail("testClient@domain.com");
        client.setPassword("testPassword");
        this.testClient = clientService.addClient(client);
    }

    private void deleteTestClient() {
        if (this.testClient != null) {
            clientService.deleteById(this.testClient.getClientId());
        }
        this.testClient = null;
    }

    @Test
    public void findById() {

        Client retrievedClient = clientService.findById(this.testClient.getClientId());

        assertNotNull(retrievedClient, "retrievedClient not null");
        assertEquals(this.testClient.getClientId(), retrievedClient.getClientId(), "clientId matches");
        assertEquals(this.testClient.getUsername(), retrievedClient.getUsername());
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(1000),
                "inexistent id search throws exception");

    }

    @Test
    public void createClient() {

        Client retrievedClient = clientService.findById(this.testClient.getClientId());

        assertNotEquals(this.testClient.getClientId(), 0, "an id has been generated");
        assertEquals(retrievedClient, this.testClient, "saved id and retrieved id match");
        assertEquals(this.testClient.getUsername(), this.testClient.getUsername(), "username saved correctly");
        assertEquals(this.testClient.getEmail(), this.testClient.getEmail(), "email saved correctly");

    }

    @Test
    public void updateClient() {
        Client newClientInfo = new Client();
        newClientInfo.setClientId(1000);
        newClientInfo.setPassword("password2");
        newClientInfo.setUsername("testClient2");
        newClientInfo.setEmail("testClient2@domain.com");

        Client updatedClient = clientService.updateClient(testClient.getClientId(), newClientInfo);

        assertEquals(testClient.getClientId(), updatedClient.getClientId(), "clientId unchanged");
        assertEquals("testClient2", updatedClient.getUsername(), "username updated correctly");
        assertEquals("testClient2@domain.com", updatedClient.getEmail(), "email updated correctly");

        assertThrows(ResourceNotFoundException.class, () -> clientService.updateClient(1000, newClientInfo),
                "updating an inexistent clientId throws exception");
    }

    @Test
    public void partiallyUpdateClient() {
        Client newClientInfo = new Client();
        newClientInfo.setClientId(1000);
        newClientInfo.setUsername("testClient2");

        Client updatedClient = clientService.partiallyUpdateClient(testClient.getClientId(), newClientInfo);

        assertEquals(testClient.getClientId(), updatedClient.getClientId(), "clientId unchanged");
        assertEquals("testClient2", updatedClient.getUsername(), "username updated correctly");
        assertEquals("testClient@domain.com", updatedClient.getEmail(), "email unchanged");

        assertThrows(ResourceNotFoundException.class, () -> clientService.partiallyUpdateClient(1000, newClientInfo),
                "updating an inexistent clientId throws exception");
    }

    @Test
    void deleteClient() {
        Client deletedClient = clientService.deleteById(this.testClient.getClientId());

        assertEquals(this.testClient.getClientId(), deletedClient.getClientId(), "client id correct");
        assertEquals("testClient", deletedClient.getUsername(), "username correct");
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(deletedClient.getClientId()),
                "clientId no longer found");

        this.testClient = null;
    }

    @Test
    void findAll() {

        Client[] sampleClients = {new Client(), new Client(), new Client()};
        for (int i = 0; i < 3; i ++) {
            Client client = sampleClients[i];
            client.setPassword("pwd" + i);
            client.setUsername("client" + i);
            client.setEmail("email" + i);
            sampleClients[i] = clientService.addClient(client);
        }

        List<Client> retrievedClients = clientService.findAll();
        Client lastClient = retrievedClients.get(retrievedClients.size() - 1);

        assertTrue(retrievedClients.size() >= 3, "there are at least three entries");
        assertEquals("email2", lastClient.getEmail(), "last email is correct");
        assertEquals("client2", lastClient.getUsername(), "last username is correct");
        assertNotNull(lastClient.getPassword(), "last password is not null");

        for (int i = 0; i < 3; i ++) {
            clientService.deleteById(sampleClients[i].getClientId());
        }
    }
}
