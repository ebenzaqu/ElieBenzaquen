package com.app.storage.model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long hostId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "address")
    private String address;

    @OneToMany
    @Column(name = "open_listings")
    private Set<Post> openListings;

    @OneToMany
    @Column(name = "booked_listings")
    private Set<Post> bookedListings;

    public Host(){}

    public Host(String user, String pass, String addr){
        this.username = user;
        this.password = pass;
        this.address = addr;
        this.openListings = new HashSet<>();
        this.bookedListings = new HashSet<>();
    }

    public void addListing(Post listing) {
        this.openListings.add(listing);
    }


    public void bookListing(Post listing) { }


    public void unbookListing(Post listing) { }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Host [address=" + address + ", id=" + hostId + ", password=" + password + ", username=" + username + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, hostId, password, username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Host))
            return false;
        Host other = (Host) obj;
        return Objects.equals(address, other.address) && hostId == other.hostId && Objects.equals(password, other.password)
                && Objects.equals(username, other.username);
    }

    public Set<Post> getOpenListings() {
        return openListings;
    }

    public void setOpenListings(Set<Post> listings) {
        this.openListings = listings;
    }

    public long getId() {
        return hostId;
    }

    public void setId(long id) {
        this.hostId = id;
    }

    public Set<Post> getBookedListings() {
        return bookedListings;
    }

    public void setBookedListings(Set<Post> bookedListings) {
        this.bookedListings = bookedListings;
    }

    

    
}
