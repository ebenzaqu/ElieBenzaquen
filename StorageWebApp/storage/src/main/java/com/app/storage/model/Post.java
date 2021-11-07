package com.app.storage.model;

import java.util.Date;
import java.util.Objects;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.jetbrains.annotations.NotNull;

@Entity
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", nullable = false, updatable = false)
    @CreationTimestamp
    private Date createDate;

    @Column(name = "startDate", nullable = false)
    private String startDate;

    @Column(name = "endDate", nullable = false)
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name="address")
    private String address;
    @Column(name="length")
    private int length;
    @Column(name="width")
    private int width;
    @Column(name="height")
    private int height;
    @Column(name="pricing")
    private int pricing;
    // @Column(name="host")
    // private Host host;
    @Column(name="booked")
    private boolean booked;

    @ManyToOne
    @JoinColumn(name="client")
    private Client client;

    public Post(){}

    public Post(String address, int length, int width, int height, int pricing) {
        this.address = address;
        this.height = height;
        this.length = length;
        this.width = width;
        this.pricing = pricing;
        // this.host = host;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPricing() {
        return pricing;
    }

    public void setPricing(int pricing) {
        this.pricing = pricing;
    }

    // public Host getHost() {
    //     return host;
    // }

    // public void setHost(Host host) {
    //     this.host = host;
    // }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "Post [address=" + address + ", booked=" + booked + ", client=" + client + ", createDate=" + createDate
                + ", height=" + height + ", id=" + id + ", length=" + length + ", pricing=" + pricing + ", user=" + user
                + ", width=" + width + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, booked, height, id, length, pricing, width);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Post))
            return false;
        Post other = (Post) obj;
        return Objects.equals(address, other.address) && booked == other.booked && height == other.height
                && Objects.equals(id, other.id) && length == other.length && pricing == other.pricing
                && width == other.width;
    }

    

    
    
}
