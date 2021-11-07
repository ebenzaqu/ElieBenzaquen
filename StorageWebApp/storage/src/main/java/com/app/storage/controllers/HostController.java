package com.app.storage.controllers;

import java.util.*;

import com.app.storage.model.Host;
import com.app.storage.model.Post;
import com.app.storage.service.HostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HostController {
    
    private final HostService hostService;

    @Autowired
    public HostController(HostService hostService) {
        this.hostService = hostService;
    }

    @GetMapping("/index")
    public ModelAndView getHosts(Model model) {
        ModelAndView modelAndView = new ModelAndView("/index");
        List<Host> host = hostService.getHost();
        System.out.println(host.toString());
        modelAndView.addObject("message", host.toString());
        return modelAndView;
    }

    @GetMapping("/register")
    public String registerHost(Model model) {
        model.addAttribute("host", new Host());
        return "/registration";
    }

    @PostMapping("/process_register")
    public String proccessRegistration(Host host) {
        hostService.addNewHost(host);
        return "/registration";
    }

    @GetMapping("/removeHost/{hostId}")
    public ModelAndView removeHost(@PathVariable("hostId") long id){
        ModelAndView modelAndView = new ModelAndView("/index");
        hostService.deleteHost(id);
        return modelAndView;
    }

    // @DeleteMapping("/{hostId}")
    // public void deleteHost(@PathVariable("hostId") Long id){
    //     hostService.deleteHost(id);
    // }

    @GetMapping("/index/{hostId}")
    public ModelAndView getSingleHost(@PathVariable("hostId") long id) {
        ModelAndView modelAndView = new ModelAndView("/index");
        System.out.println(hostService.findById(id).toString());
        return modelAndView;
    }

    @GetMapping("/addlisting/{hostId}")
    public String listingPage(@PathVariable("hostId") long id, Model model) {
        model.addAttribute("listing", new Post());
        model.addAttribute("id", id);
        return "/add_listing";
    }

    @PostMapping("listing_register")
    public String addListing(Post listing) {
        System.out.println(listing.toString());
        // System.out.println(id);
        // hostService.findById(id).addListing(listing);
        return "/add_listing";
    }

    // @PutMapping("/index/{id}")
    // public void updateHost(@RequestBody Host host, @PathVariable long id) {
        
    // }

    // @DeleteMapping("index/{id}")
    // public void deleteHost(@PathVariable long id) {
    //     hostService.deleteHost(id);
    // }

}
