package com.dogginer.dog.controller;

import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;
import com.dogginer.dog.service.IClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
    public List<Client> getAllClients() {
        logger.debug("Received GET request at endpoint /clients");
        return clientService.findAll();
    }

    @GetMapping("/{clientId}")
    public EntityModel<Client> getClient(@PathVariable int clientId) {
        logger.debug("Received GET request at endpoint /clients/" + clientId);
        Client client = clientService.findById(clientId);
        if (client == null) throw new ResourceNotFoundException("clienId:" + clientId);

        EntityModel<Client> entityModel = EntityModel.of(client);
        WebMvcLinkBuilder getAllClientsLink = linkTo(methodOn(this.getClass()).getAllClients());
        entityModel.add(getAllClientsLink.withRel("all-clients"));
        return entityModel;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
        produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        logger.debug("Received POST request at endpoint /clients");
        Client createdClient = clientService.addClient(client);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdClient.getClientId())
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(client, headers, HttpStatus.CREATED);
    }

    @PutMapping(path="/{clientId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateClient(@PathVariable int clientId, @RequestBody Client client) {
        logger.debug("Received PUT request at endpoint /clients/" + clientId);
        clientService.updateClient(clientId, client);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(clientId)
                .toUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(location);
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.NO_CONTENT);
    }

    @PatchMapping(path="/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Client> partiallyUpdateClient(@PathVariable int clientId, @RequestBody Client client) {
        logger.debug("Received PATCH request at endpoint /clients/" + clientId);
        Client updatedClient = clientService.partiallyUpdateClient(clientId, client);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(clientId)
                .toUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(location);
        return new ResponseEntity<>(updatedClient, httpHeaders, HttpStatus.OK);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Client> deleteClient(@PathVariable int clientId) {
        logger.debug("Received DELETE request at endpoint /clients/" + clientId);
        Client deletedClient = clientService.deleteById(clientId);
        return new ResponseEntity<>(deletedClient, HttpStatus.OK);
    }
}
