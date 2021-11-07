package com.app.storage.service.impl;

import java.util.*;

import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;
import com.app.storage.repository.PostRepository;
import com.app.storage.service.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }
    
    @Override
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public void deletePostById(Long id) {
        if(!postRepository.existsById(id)){
            throw new IllegalStateException("Error: Listing does not exist");
        } 
        postRepository.deleteById(id);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    @Override
    public Page<Post> findByUserOrderedByDatePageable(User user, int page) {
        return postRepository.findByUserOrderByCreateDateDesc(user, PageRequest.of(subtractPageByOne(page), 5));
    }

    @Override
    public Page<Post> findByClientOrderedByDatePageable(Client client, int page) {
        return postRepository.findByClientOrderByCreateDateDesc(client, PageRequest.of(subtractPageByOne(page), 5));
    }

    @Override
    public Page<Post> findByBookedFalseOrderedByDatePageable(int page) {
        return postRepository.findByBookedFalseOrderByCreateDateDesc(PageRequest.of(subtractPageByOne(page), 5));
    }

    private int subtractPageByOne(int page){
        return (page < 1) ? 0 : page - 1;
    }

}
