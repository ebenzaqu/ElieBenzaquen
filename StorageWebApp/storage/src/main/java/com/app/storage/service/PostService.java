package com.app.storage.service;

import java.util.List;
import java.util.Optional;

import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;

import org.springframework.data.domain.Page;

public interface PostService {

    List<Post> getAllPosts();

    Optional<Post> findById(Long id);

    Post save(Post post);

    /**
     * Finds a {@link Page) of {@link Post} of provided user ordered by date
     */
    Page<Post> findByUserOrderedByDatePageable(User user, int page);

    /**
     * Finds a {@link Page) of {@link Post} of provided user ordered by date
     */
    Page<Post> findByClientOrderedByDatePageable(Client client, int page);

    /**
     * Finds a {@link Page) of all {@link Post} ordered by date
     */
    Page<Post> findByBookedFalseOrderedByDatePageable(int page);

    void delete(Post post);
}