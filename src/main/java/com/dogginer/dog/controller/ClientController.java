package com.dogginer.dog.controller;

import com.dogginer.dog.exception.ClientNotFoundException;
import com.dogginer.dog.service.IClientService;
import com.dogginer.dog.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.print.attribute.standard.Media;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
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
    public ResponseEntity<Client> getClient(@PathVariable int clientId) {
        logger.debug("Received GET request at endpoint /clients/" + clientId);
        Client client = clientService.findById(clientId);

        if (client == null) throw new ClientNotFoundException("id:" + clientId);
        return new ResponseEntity<Client>(client,HttpStatus.OK);
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
//        return ResponseEntity.created(location).build(); //return the uri of the new resource
        headers.setLocation(location);
        return new ResponseEntity<>(client, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Object> deleteClient(@PathVariable int clientId) {
        logger.debug("Received DELETE request at endopoint /clients/" + clientId);
        Client deletedClient = clientService.deleteById(clientId);
        return new ResponseEntity<>(deletedClient, HttpStatus.OK);
    }
}
