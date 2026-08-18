package com.lifesync.authservice.repository;

import com.lifesync.authservice.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    
    // Allows us to find a user by their email address
    Optional<AppUser> findByEmail(String email);
}