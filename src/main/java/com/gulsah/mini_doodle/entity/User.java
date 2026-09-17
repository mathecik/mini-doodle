package com.gulsah.mini_doodle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doodle_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    protected User() {}

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
