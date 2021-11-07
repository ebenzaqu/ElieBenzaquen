package com.app.storage.repository;

import java.util.Optional;

import com.app.storage.model.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

    Optional<Client> findById(Long id);
    Client findByUsername(String username);
}