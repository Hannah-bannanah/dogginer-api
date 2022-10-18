package com.dogginer.dog.controller;

import com.dogginer.dog.model.Client;
import com.dogginer.dog.service.IClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("v1/clients")
public class ClientController {

    private IClientService clientService;
    private static Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired public ClientController(IClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping()
    public CollectionModel<Client> getAllClients() {
        logger.debug("Received GET request at endpoint /clients");
        List<Client> clients = clientService.findAll()
                .stream()
                .map(client ->
                        client.add(linkTo(methodOn(this.getClass()).getClient(client.getClientId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(clients);
    }

    @GetMapping("/{clientId}")
    public Client getClient(@PathVariable int clientId) {
        logger.debug("Received GET request at endpoint v1/clients/" + clientId);
        Client client = clientService.findById(clientId);

        WebMvcLinkBuilder getAllClientsLink = linkTo(methodOn(this.getClass()).getAllClients());
        client.add(getAllClientsLink.withRel("all-clients"));

        return client;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
        produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Client createClient(@RequestBody Client client) {
        logger.debug("Received POST request at endpoint v1/clients");

        Client createdClient = clientService.addClient(client);

        createdClient.add(linkTo(methodOn(this.getClass()).getAllClients()).withRel("all-clients"));
        createdClient.add(linkTo(methodOn(this.getClass()).getClient(createdClient.getClientId())).withSelfRel());

        return createdClient;
    }

    @PutMapping(path="/{clientId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces= MediaType.APPLICATION_JSON_VALUE)
    public Client updateClient(@PathVariable int clientId, @RequestBody Client client) {
        logger.debug("Received PUT request at endpoint v1/clients/" + clientId);

        Client updatedClient = clientService.updateClient(clientId, client);

        updatedClient.add(linkTo(methodOn(this.getClass()).getAllClients()).withRel("all-clients"));
        updatedClient.add(linkTo(methodOn(this.getClass()).getClient(clientId)).withSelfRel());

        return updatedClient;
    }

    @PatchMapping(path="/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Client partiallyUpdateClient(@PathVariable int clientId, @RequestBody Client client) {
        logger.debug("Received PATCH request at endpoint v1/clients/" + clientId);

        Client updatedClient = clientService.partiallyUpdateClient(clientId, client);

        updatedClient.add(linkTo(methodOn(this.getClass()).getAllClients()).withRel("all-clients"));
        updatedClient.add(linkTo(methodOn(this.getClass()).getClient(clientId)).withSelfRel());

        return updatedClient;
    }

    @DeleteMapping("/{clientId}")
    @Transactional
    public Client deleteClient(@PathVariable int clientId) {
        logger.debug("Received DELETE request at endpoint v1/clients/" + clientId);

        Client deletedClient = clientService.deleteById(clientId);

        deletedClient.add(linkTo(methodOn(this.getClass()).getAllClients()).withRel("all-clients"));
        deletedClient.add(linkTo(methodOn(this.getClass()).getAllClients()).withRel("all-clients"));

        return deletedClient;
    }
}
