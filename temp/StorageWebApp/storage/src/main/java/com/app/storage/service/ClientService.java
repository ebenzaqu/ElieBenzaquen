package com.app.storage.service;

import java.util.List;
import java.util.Optional;


import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;

import org.springframework.data.domain.Page;

public interface ClientService {

    List<Client> getAllClients();

    Optional<Client> findById(Long id);

    Client findByUsername(String username);

    Client save(Client post);

    void delete(Client post);
}