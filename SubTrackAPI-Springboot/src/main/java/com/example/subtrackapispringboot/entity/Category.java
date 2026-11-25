package com.example.subtrackapispringboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;


@Entity
@Table(name= "categories")
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(unique = true, nullable = false, length = 50)
    public String name = "";

    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    public List<Subscription> subscriptions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
