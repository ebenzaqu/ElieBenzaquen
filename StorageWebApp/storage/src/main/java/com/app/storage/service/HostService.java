package com.app.storage.service;

import java.util.*;

import com.app.storage.model.Host;
import com.app.storage.repository.HostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostService {
    
    private final HostRepository hostRepository;

    @Autowired
    public HostService(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public List<Host> getHost() {
        return hostRepository.findAll();
    }

    public void addNewHost(Host host) {
        hostRepository.save(host);
    }

    public Host findById(long id) {
        return hostRepository.getById(id);
    }

    public void deleteHost(Long id) {
        if(!hostRepository.existsById(id)){
            throw new IllegalStateException("Error: Host does not exist");
        } 
        hostRepository.deleteById(id);
    }

}
