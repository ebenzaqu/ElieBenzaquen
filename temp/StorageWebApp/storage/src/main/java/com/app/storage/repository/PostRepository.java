package com.app.storage.repository;

import java.util.Optional;

import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{

    Page<Post> findByUserOrderByCreateDateDesc(User user, Pageable pageable);

    Page<Post> findByClientOrderByCreateDateDesc(Client client, Pageable pageable);

    Page<Post> findByBookedFalseOrderByCreateDateDesc(Pageable pageable);

    Optional<Post> findById(Long id);
    
}
