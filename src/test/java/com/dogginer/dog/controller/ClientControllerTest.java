package com.dogginer.dog.controller;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;
import com.dogginer.dog.service.IClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClientController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IClientService clientService;

    private ObjectMapper objectMapper;

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

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void afterEach() {
        this.clientList = new ArrayList<>();
    }

    @Test
    void getAllClients() throws Exception {
        when(clientService.findAll()).thenReturn(clientList);

        mockMvc.perform(get("/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clientList", hasSize(3)))
                .andExpect(jsonPath("$._embedded.clientList[0].clientId").value("1"))
                .andExpect(jsonPath("$._embedded.clientList[0].username").value("client0"))
                .andExpect(jsonPath("$._embedded.clientList[0].email").value("email0"))
                .andExpect(jsonPath("$._embedded.clientList[0].password").doesNotExist())
                .andExpect(jsonPath("$._embedded.clientList[0]._links").exists())
                .andExpect(jsonPath("$._embedded.clientList[0]._links.self.href").value("http://localhost/v1/clients/1"))
                .andExpect(jsonPath("$._embedded.clientList[1].clientId").value("2"))
                .andExpect(jsonPath("$._embedded.clientList[1].username").value("client1"))
                .andExpect(jsonPath("$._embedded.clientList[1].email").value("email1"))
                .andExpect(jsonPath("$._embedded.clientList[1].password").doesNotExist())
                .andExpect(jsonPath("$._embedded.clientList[1]._links").exists())
                .andExpect(jsonPath("$._embedded.clientList[1]._links.self.href").value("http://localhost/v1/clients/2"))
                .andExpect(jsonPath("$._embedded.clientList[2].clientId").value("3"))
                .andExpect(jsonPath("$._embedded.clientList[2].username").value("client2"))
                .andExpect(jsonPath("$._embedded.clientList[2].email").value("email2"))
                .andExpect(jsonPath("$._embedded.clientList[2].password").doesNotExist())
                .andExpect(jsonPath("$._embedded.clientList[2]._links").exists())
                .andExpect(jsonPath("$._embedded.clientList[2]._links.self.href").value("http://localhost/v1/clients/3"));
    }

    @Test
    void getClient() throws Exception {
        Client testClient = this.createTestClient();
        when(clientService.findById(1)).thenReturn(testClient);
        when(clientService.findById(0)).thenThrow(new ResourceNotFoundException("clientId:0"));

        mockMvc.perform(get("/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testClient1"))
                .andExpect(jsonPath("$.email").value("testClient1@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/v1/clients/0"))
                .andExpect(status().isNotFound());

    }


    private Client createTestClient() {
        Client testClient = new Client();
        testClient.setClientId(1);
        testClient.setUsername("testClient1");
        testClient.setEmail("testClient1@email.com");
        testClient.setPassword("$2a$12$ywLMR6oIctBgnBhk9p8xUubZRpPpOE5nIwQz855QNRQioq4JMJEFW");
        return testClient;
    }

    @Test
    void createClient() throws Exception {
        Client newClient = this.createTestClient();
        newClient.setClientId(null);
        Client wrongClient = this.createTestClient();
        wrongClient.setClientId(0);

        when(clientService.addClient(ArgumentMatchers.any())).then(invocation -> {
            Client savedClient = invocation.getArgument(0);
            if (savedClient.getClientId() != null && savedClient.getClientId() == 0)
                throw new BadRequestException("Bad request");
            savedClient.setClientId(1);
            return savedClient;
        });

        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId", is(1)))
                .andExpect(jsonPath("$.username", is("testClient1")))
                .andExpect(jsonPath("$.email", is("testClient1@email.com")))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongClient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateClient() throws Exception {
        Client updatedClient = this.createTestClient();
        updatedClient.setUsername("updatedUsername");
        updatedClient.setEmail("updatedEmail@email.com");
        updatedClient.setPassword("updatedPassword");

        when(clientService.updateClient(updatedClient.getClientId(), updatedClient))
                .thenReturn(updatedClient);
        //mock wrong data response
        when(clientService.updateClient(100, updatedClient)).thenThrow(new BadRequestException("Bad request"));
        //mock inexistent clientId response
        when(clientService.updateClient(0, updatedClient)).thenThrow(new ResourceNotFoundException("clientId:0"));


        mockMvc.perform(put("/v1/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("1"))
                .andExpect(jsonPath("$.username").value("updatedUsername"))
                .andExpect(jsonPath("$.email").value("updatedEmail@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(put("/v1/clients/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/v1/clients/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isNotFound());
    }

    @Test
    void partiallyUpdateClient() throws Exception {
        Client updatedEmailClient = this.createTestClient();
        Client email = new Client();
        email.setEmail("newEmail@email.com");
        updatedEmailClient.setEmail(email.getEmail());

        Client updatedUsernameClient = this.createTestClient();
        Client username = new Client();
        username.setUsername("newUsername");
        updatedUsernameClient.setUsername(username.getUsername());

        Client updatedPasswordClient = this.createTestClient();
        Client password = new Client();
        password.setPassword("newPassword");
        updatedPasswordClient.setPassword(password.getPassword());

        when(clientService.partiallyUpdateClient(anyInt(), ArgumentMatchers.any(Client.class))).then(invocation -> {
            int clientId = invocation.getArgument(0);
            if (clientId == 100) throw new BadRequestException("Bad request"); // test wrong data
            if (clientId == 0) throw new ResourceNotFoundException("clientId:0"); // test inexistent client
            Client update = invocation.getArgument(1);
            Client updatedClient = this.createTestClient();
            updatedClient.setClientId(clientId);
            if (StringUtils.isNotEmpty(update.getPassword())) updatedClient.setPassword(update.getPassword());
            if (StringUtils.isNotEmpty(update.getEmail())) updatedClient.setEmail(update.getEmail());
            if (StringUtils.isNotEmpty(update.getUsername())) updatedClient.setUsername(update.getUsername());

            return updatedClient;
        });

        mockMvc.perform(patch("/v1/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(1)))
                .andExpect(jsonPath("$.username", is("testClient1")))
                .andExpect(jsonPath("$.email", is("newEmail@email.com")))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(patch("/v1/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(username)))
                .andExpect(status().isOk());


        mockMvc.perform(patch("/v1/clients/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(3)))
                .andExpect(jsonPath("$.username", is("testClient1")))
                .andExpect(jsonPath("$.email", is("testClient1@email.com")))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(patch("/v1/clients/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/v1/clients/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient() throws Exception {

        when(clientService.deleteById(1)).thenReturn(this.createTestClient());
        when(clientService.deleteById(0)).thenThrow(new ResourceNotFoundException("clientId:0"));

        mockMvc.perform(delete("/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("1"))
                .andExpect(jsonPath("$.username").value("testClient1"))
                .andExpect(jsonPath("$.email").value("testClient1@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mockMvc.perform(delete("/v1/clients/0"))
                .andExpect(status().isNotFound());

    }
}