package com.dogginer.dog.service;

import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.model.Client;

import java.util.List;

public interface IClientService {
    List<Client> findAll();

    Client findById(int clientId);

    Client addClient(Client client);

    Client deleteById(int clientId) throws ResourceNotFoundException;

    void updateClient(int clientId, Client client);

    Client partiallyUpdateClient(int clientId, Client client);
}
