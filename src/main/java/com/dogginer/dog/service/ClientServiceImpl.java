package com.dogginer.dog.service;

import com.dogginer.dog.exception.ClientNotFoundException;
import com.dogginer.dog.repository.IClientRepository;
import com.dogginer.dog.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for the Clients in the dogginer
 * @author hannah-bannanah
 *
 */

@Service
public class ClientServiceImpl implements IClientService{
    private IClientRepository clientDao;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired public ClientServiceImpl(IClientRepository clientDao, BCryptPasswordEncoder passwordEncoder) {
        this.clientDao = clientDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all the clients in dogginer
     * @return a list of all clients
     */
    @Override
    public List<Client> findAll() {
        return clientDao.findAll();
    }

    /**
     * Search for a client by clientId
     * @param clientId
     * @return the client object if it exists, null otherwise
     */
    @Override
    public Client findById(int clientId) {
        return clientDao.findById(clientId).orElse(null);
    }

    /**
     * Creates a new client
     * @param client an object with all client data
     * @return the created client object
     */
    @Override
    public Client addClient(Client client) {
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        Client savedClient = clientDao.save(client);
        return savedClient;
    }

    @Override
    public Client deleteById(int clientId) {
        Optional<Client> client = clientDao.findById(clientId);
        if (!client.isPresent()) throw new ClientNotFoundException("id:" + clientId);
        clientDao.deleteById(clientId);
        return client.get();
    }
}
