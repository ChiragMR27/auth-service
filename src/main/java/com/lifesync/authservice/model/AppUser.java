package com.lifesync.authservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // NEW: Stores the user's email natively in the database!
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; 
}