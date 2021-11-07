package com.app.storage.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.Optional;

import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;
import com.app.storage.service.BookedService;
import com.app.storage.service.ClientService;
import com.app.storage.service.PostService;
import com.app.storage.service.UserService;

@Controller
public class PostController {

    private final PostService postService;
    private final ClientService clientService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.postService = postService;
        this.userService = userService;
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.GET)
    public String newPost(Principal principal, Model model) {

        if(principal == null){
            return "/error";
        }

        Optional<User> user = userService.findByUsername(principal.getName());

        if (user.isPresent()) {
            Post post = new Post();
            post.setUser(user.get());

            model.addAttribute("post", post);

            return "/post_form";

        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.POST)
    public String createNewPost(Post post, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/post_form";
        } else {
            postService.save(post);
            post.getUser().addPost(post);
            return "redirect:/units/" + post.getUser().getUsername();
        }
    }

    @RequestMapping(value = "/editPost/{id}", method = RequestMethod.GET)
    public String editPostWithId(@PathVariable Long id,
                                 Principal principal,
                                 Model model) {

        Optional<Post> optionalPost = postService.findById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            if (isPrincipalOwnerOfPost(principal, post)) {
                model.addAttribute("post", post);
                return "/post_form";
            } else {
                return "/403";
            }

        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.GET)
    public String getPostWithId(@PathVariable Long id,
                                Principal principal,
                                Model model) {

        Optional<Post> optionalPost = postService.findById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            model.addAttribute("post", post);
            if (isPrincipalOwnerOfPost(principal, post)) {
                model.addAttribute("username", principal.getName());
            }

            return "/post";

        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.POST)
    public String deletePostWithId(@PathVariable Long id,
                                   Principal principal) {

        Optional<Post> optionalPost = postService.findById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            if (isPrincipalOwnerOfPost(principal, post)) {
                postService.delete(post);
                return "redirect:/home";
            } else {
                return "/403";
            }

        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/book/{id}", method = RequestMethod.GET)
    public String bookWithId(@PathVariable Long id,
                                   Principal principal) {

        Optional<Post> optionalPost = postService.findById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            if (isPrincipalOwnerOfPost(principal, post)) {
                return "/error";
            }

            post.setBooked(true);
            post.setClient(clientService.findByUsername(principal.getName()));
            postService.save(post);

            System.out.println(post.toString());

            return "redirect:/bookings/" + principal.getName();

        } else {
            return "/error";
        }
    }

    private boolean isPrincipalOwnerOfPost(Principal principal, Post post) {
        return principal != null && principal.getName().equals(post.getUser().getUsername());
    }
}