package com.lifesync.authservice.controller;

import com.lifesync.authservice.dto.AuthRequest;
import com.lifesync.authservice.dto.AuthResponse;
import com.lifesync.authservice.model.AppUser;
import com.lifesync.authservice.repository.UserRepository;
import com.lifesync.authservice.security.JwtUtil;
import com.lifesync.authservice.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; 

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                          UserRepository userRepository, PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        String otp = String.format("%04d", new Random().nextInt(10000));
        otpStorage.put(email, otp);

        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok("OTP sending initiated for " + email);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String userOtp = request.get("otp");

        String validOtp = otpStorage.get(email);
        if (validOtp == null || !validOtp.equals(userOtp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP!");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email); 
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); 

        userRepository.save(user);
        otpStorage.remove(email);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/direct-register")
    public ResponseEntity<String> directRegisterUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String role = request.get("role");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already registered!");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        user.setRole(role != null && !role.trim().isEmpty() ? role : "USER"); 

        userRepository.save(user);

        return ResponseEntity.ok("User created successfully via Admin Panel!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        
        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        String token = jwtUtil.generateToken(user.getUsername());
        
       
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUserProfile(@RequestBody Map<String, String> request) {
        String currentIdentifier = request.get("currentUsername");
        String newIdentifier = request.get("newUsername");
        String newPassword = request.get("newPassword");

        Optional<AppUser> optionalUser = userRepository.findByUsername(currentIdentifier);
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByEmail(currentIdentifier);
        }

        return optionalUser.map(user -> {
            if (newIdentifier != null && !newIdentifier.trim().isEmpty()) {
                if (newIdentifier.contains("@")) {
                    if (!newIdentifier.equals(user.getEmail()) && userRepository.findByEmail(newIdentifier).isPresent()) {
                        return ResponseEntity.badRequest().body("That email is already in use!");
                    }
                    user.setEmail(newIdentifier);
                } else {
                    if (!newIdentifier.equals(user.getUsername()) && userRepository.findByUsername(newIdentifier).isPresent()) {
                        return ResponseEntity.badRequest().body("That username is already taken!");
                    }
                    user.setUsername(newIdentifier);
                }
            }
            
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
            
            userRepository.save(user);
            return ResponseEntity.ok("Profile updated successfully");
        }).orElse(ResponseEntity.badRequest().body("User not found in database. Try logging in again!"));
    }

    // NEW: Check if a user exists before adding them to a group
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(email).isPresent();
        return ResponseEntity.ok(exists);
    }
}