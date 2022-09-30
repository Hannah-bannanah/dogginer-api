package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ClientNotFoundException;
import com.dogginer.dog.repository.IClientRepository;
import com.dogginer.dog.model.Client;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

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

    /**
     * Delete an existing client
     * @param clientId
     * @return the deleted object if the operation was successful, null otherwise
     * @throws ClientNotFoundException
     */
    @Override
    public Client deleteById(int clientId) {
        Optional<Client> client = clientDao.findById(clientId);
        if (!client.isPresent()) throw new ClientNotFoundException("id:" + clientId);
        clientDao.deleteById(clientId);
        return client.get();
    }

    /**
     * Replaces an existing client object with another one
     * @param clientId the id of the client to be replaced
     * @param client the new client object
     */
    @Override
    public void updateClient(int clientId, Client client) {
        if ((Integer) client.getClientId() != null && clientId != client.getClientId())
            throw new BadRequestException("resource uri and clientId do not match");

        Optional<Client> existingClient = clientDao.findById(clientId);
        if (!existingClient.isPresent()) throw new ClientNotFoundException("id:" + clientId);

        clientDao.save(client);
    }

    /**
     * Makes partial updates to an existing client
     * @param clientId the id of the client to be replaced
     * @param update an object with the fields to be modified
     */
    @Override
    public Client partiallyUpdateClient(int clientId, Client update) {
        Client existingClient = clientDao.findById(clientId).orElse(null);
        if (existingClient == null) throw new ClientNotFoundException("id:" + clientId);

        // we check if there are changes in the update object
        boolean isUpdate = false;

        if (StringUtils.isNotEmpty(update.getEmail())
                && !StringUtils.equalsIgnoreCase(update.getEmail(), existingClient.getEmail())) {
            existingClient.setEmail(update.getEmail());
            isUpdate = true;
        }

        if (StringUtils.isNotEmpty(update.getUsername())
                && !StringUtils.equalsIgnoreCase(update.getUsername(), existingClient.getUsername())) {
            existingClient.setUsername(update.getUsername());
            isUpdate = true;
        }

        if (StringUtils.isNotEmpty(update.getPassword())
                && !passwordEncoder.matches(update.getPassword(), existingClient.getPassword())) {
            System.out.println("passwords dont match");
            existingClient.setPassword(passwordEncoder.encode(update.getPassword()));
            isUpdate = true;
        }

        if (!isUpdate) return null;

        try {
            clientDao.save(existingClient);
        } catch (Exception e) {
            logger.error("There was an error in the method updateUser() in class " + this.getClass().getSimpleName() + ". The trace is: ", e);
            throw new BadRequestException("The update could not take place");
        }
        return existingClient;
    }

}
