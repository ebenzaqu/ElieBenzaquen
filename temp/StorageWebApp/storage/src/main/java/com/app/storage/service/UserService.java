package com.app.storage.service;

import java.util.Optional;

import com.app.storage.model.User;

public interface UserService {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User saveUser(User user);

}