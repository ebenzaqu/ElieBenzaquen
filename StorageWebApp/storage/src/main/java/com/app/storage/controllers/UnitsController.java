package com.app.storage.controllers;

import com.app.storage.model.Client;
import com.app.storage.model.Post;
import com.app.storage.model.User;
import com.app.storage.service.ClientService;
import com.app.storage.service.PostService;
import com.app.storage.service.UserService;
import com.app.storage.util.Pager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@Controller
public class UnitsController {

    private final UserService userService;
    private final ClientService clientService;
    private final PostService postService;

    @Autowired
    public UnitsController(UserService userService, ClientService clientService, PostService postService) {
        this.userService = userService;
        this.clientService = clientService;
        this.postService = postService;
    }

    @RequestMapping(value = "/units", method = RequestMethod.GET)
    public String allUnits(@RequestParam(defaultValue = "0") int page,
                            Model model){

        Page<Post> posts = postService.findByBookedFalseOrderedByDatePageable(page);
        Pager pager = new Pager(posts);

        model.addAttribute("pager", pager);

        return "/allPosts";

    }

    @RequestMapping(value = "/units/{username}", method = RequestMethod.GET)
    public String unitsForUsername(@PathVariable String username,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {

        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Page<Post> posts = postService.findByUserOrderedByDatePageable(user, page);
            Pager pager = new Pager(posts);

            model.addAttribute("pager", pager);
            model.addAttribute("user", user);

            return "/posts";

        } else {
            return "/error";
        }
    }

    @RequestMapping(value = "/myPosts", method = RequestMethod.GET)
    public String getUnitsForUsername(Principal principal) {
        return "redirect:/units/" + principal.getName();
    }

    @RequestMapping(value = "/bookings/{username}", method = RequestMethod.GET)
    public String unitsForClient(@PathVariable String username,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {

        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Page<Post> posts = postService.findByClientOrderedByDatePageable(clientService.findByUsername(user.getUsername()), page);
            Pager pager = new Pager(posts);

            model.addAttribute("pager", pager);
            model.addAttribute("user", user);

            return "/posts";

        } else {
            return "/error";
        }
    }
    
}
