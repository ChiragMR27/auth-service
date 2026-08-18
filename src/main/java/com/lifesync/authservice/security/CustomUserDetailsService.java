package com.lifesync.authservice.security;

import com.lifesync.authservice.model.AppUser;
import com.lifesync.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // THE FIX: Check if the identifier matches a username first
        Optional<AppUser> userOptional = userRepository.findByUsername(identifier);
        
        // If not found, check if it matches an email instead
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        // If neither exists, throw the error
        AppUser appUser = userOptional.orElseThrow(() -> 
                new UsernameNotFoundException("User not found with identifier: " + identifier));

        // Return the valid user to Spring Security
        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                Collections.emptyList() 
        );
    }
}