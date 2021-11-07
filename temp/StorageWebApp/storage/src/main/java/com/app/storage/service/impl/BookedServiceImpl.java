package com.app.storage.service.impl;

import java.util.*;

import com.app.storage.model.Post;
import com.app.storage.model.User;
import com.app.storage.repository.BookedRepository;
import com.app.storage.service.BookedService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class BookedServiceImpl implements BookedService{

    private final BookedRepository bookedRepository;

    @Autowired
    public BookedServiceImpl(BookedRepository postRepository){
        this.bookedRepository = postRepository;
    }

    @Override
    public List<Post> getAllPosts() {
        return bookedRepository.findAll();
    }

    public Post save(Post post) {
        return bookedRepository.save(post);
    }
    
    @Override
    public Optional<Post> findById(Long id) {
        return bookedRepository.findById(id);
    }

    public void deletePostById(Long id) {
        if(!bookedRepository.existsById(id)){
            throw new IllegalStateException("Error: Listing does not exist");
        } 
        bookedRepository.deleteById(id);
    }

    public void delete(Post post) {
        bookedRepository.delete(post);
    }

    @Override
    public Page<Post> findByUserOrderedByDatePageable(User user, int page) {
        return bookedRepository.findByUserOrderByCreateDateDesc(user, PageRequest.of(subtractPageByOne(page), 5));
    }

    @Override
    public Page<Post> findAllOrderedByDatePageable(int page) {
        return bookedRepository.findAllByOrderByCreateDateDesc(PageRequest.of(subtractPageByOne(page), 5));
    }

    private int subtractPageByOne(int page){
        return (page < 1) ? 0 : page - 1;
    }

}