package com.dogginer.dog.service;

import com.dogginer.dog.exception.BadRequestException;
import com.dogginer.dog.exception.ResourceNotFoundException;
import com.dogginer.dog.repository.IClientRepository;
import com.dogginer.dog.model.Client;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for the Clients in the dogginer
 * @author hannah-bannanah
 *
 */

@Service
public class ClientServiceImpl implements IClientService{
    private IClientRepository clientRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired public ClientServiceImpl(IClientRepository clientRepository, BCryptPasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all the clients in dogginer
     * @return a list of all clients
     */
    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    /**
     * Search for a client by clientId
     * @param clientId
     * @return the client object if it exists, null otherwise
     */
    @Override
    public Client findById(int clientId) {

        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("clientId:" + clientId));
    }

    /**
     * Creates a new client
     * @param client an object with all client data
     * @return the created client object
     */
    @Override
    public Client addClient(Client client) {
        client.setClientId(0);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        return this.saveClient(client);
    }

    /**
     * Delete an existing client
     * @param clientId
     * @return the deleted object if the operation was successful, null otherwise
     * @throws ResourceNotFoundException
     */
    @Override
    @Transactional
    public Client deleteById(int clientId) {
        Client deletedClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("clientId:" + clientId));
        clientRepository.deleteById(clientId);
        return deletedClient;
    }

    /**
     * Replaces an existing client object with another one
     * @param clientId the id of the client to be replaced
     * @param client the new client object
     * @throws ResourceNotFoundException
     */
    @Override
    public Client updateClient(int clientId, Client client) {
        Client existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("clientId:" + clientId));

        client.setClientId(existingClient.getClientId());
        return this.saveClient(client);
    }

    /**
     * Makes partial updates to an existing client
     * @param clientId the id of the client to be replaced
     * @param clientUpdates an object with the fields to be modified
     * @throws ResourceNotFoundException
     */
    @Override
    public Client partiallyUpdateClient(int clientId, Client clientUpdates) {
        Client updatedClient = clientRepository.findById(clientId)
                .map(existingClient -> copyNonNullFields(clientUpdates, existingClient))
                .orElseThrow(() -> new ResourceNotFoundException("clientId:" + clientId));
        return this.saveClient(updatedClient);
    }

    private Client copyNonNullFields(Client origin, Client destination) {
        if (StringUtils.isNotEmpty(origin.getUsername()))
            destination.setUsername(origin.getUsername());
        if (StringUtils.isNotEmpty(origin.getEmail()))
            destination.setEmail(origin.getEmail());
        return destination;
    }

    private Client saveClient(Client client) {
        Client updatedClient = null;
        try {
            updatedClient = clientRepository.save(client);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(e.getCause().getCause().getLocalizedMessage());
        } catch (Exception e) {
            throw new BadRequestException("Bad request");
        }

        return updatedClient;
    }
}
