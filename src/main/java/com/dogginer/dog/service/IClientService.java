package com.dogginer.dog.service;

import com.dogginer.dog.model.Client;

import java.util.List;

public interface IClientService {
    List<Client> findAll();

    Client findById(int clientId);

    Client addClient(Client client);

    Client deleteById(int clientId);
}
