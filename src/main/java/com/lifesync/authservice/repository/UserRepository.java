package com.lifesync.authservice.repository;

import com.lifesync.authservice.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    // Spring Data JPA will automatically write the SQL to find a user by their username!
    Optional<AppUser> findByUsername(String username);
}