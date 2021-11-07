package com.app.storage.model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "username")
    private String username;

    // @Column(name = "password")
    // private String password;

    // @Column(name = "booked_listings")
    // private Set<Listing> currentListings; // the listings that the client has booked

    public Client(){}

    public Client(String username) {
        this.username = username;
    }

    public long getID(){ return this.id; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // public String getPassword() {
    //     return password;
    // }

    // public void setPassword(String password) {
    //     this.password = password;
    // }

    // @Override
    // public String toString() {
    //     return "Client [id=" + id + ", password=" + password + ", username=" + username + "]";
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(id, password, username);
    // }

    // @Override
    // public boolean equals(Object obj) {
    //     if (this == obj)
    //         return true;
    //     if (!(obj instanceof Client))
    //         return false;
    //     Client other = (Client) obj;
    //     return id == other.id && Objects.equals(password, other.password) && Objects.equals(username, other.username);
    // }

    // public Set<Listing> getCurrentListings() {
    //     return currentListings;
    // }

    // public boolean addListing(Listing listing){
    //     return this.currentListings.add(listing);
    // }
    
}
