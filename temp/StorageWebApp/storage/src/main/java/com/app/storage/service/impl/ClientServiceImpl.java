package com.app.storage.service.impl;

import java.util.*;

import com.app.storage.model.Client;
import com.app.storage.repository.ClientRepository;
import com.app.storage.service.ClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService{

    private final ClientRepository clientRepository;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository){
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client findByUsername(String username){
        return clientRepository.findByUsername(username);
    }

    public Client save(Client post) {
        return clientRepository.save(post);
    }
    
    @Override
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public void deletePostById(Long id) {
        if(!clientRepository.existsById(id)){
            throw new IllegalStateException("Error: Listing does not exist");
        } 
        clientRepository.deleteById(id);
    }

    public void delete(Client post) {
        clientRepository.delete(post);
    }

}