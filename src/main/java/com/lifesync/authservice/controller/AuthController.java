package com.lifesync.authservice.controller;

import com.lifesync.authservice.dto.AuthRequest;
import com.lifesync.authservice.dto.AuthResponse;
import com.lifesync.authservice.model.AppUser;
import com.lifesync.authservice.repository.UserRepository;
import com.lifesync.authservice.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // Temporary storage for OTPs: Maps Email -> OTP Code
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                          UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JavaMailSender mailSender) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // Step 1: Generate and Send OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // Generate a random 4-digit OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpStorage.put(email, otp);

        // Send the email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("LifeSync - Your Verification Code");
        message.setText("Welcome to LifeSync! Your 4-digit registration OTP is: " + otp);
        
        try {
            mailSender.send(message);
            return ResponseEntity.ok("OTP sent to " + email);
        } catch (Exception e) {
            e.printStackTrace(); // <-- ADD THIS LINE! This will print the exact Google error to your console.
            return ResponseEntity.internalServerError().body("Failed to send email. Check configuration.");
        }
    }

    // Step 2: Verify OTP and Save User
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String userOtp = request.get("otp");

        // Check if OTP matches
        String validOtp = otpStorage.get(email);
        if (validOtp == null || !validOtp.equals(userOtp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP!");
        }

        // OTP is valid, save the user
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Encrypt before saving
        user.setRole("USER"); 

        userRepository.save(user);
        
        // Remove OTP from temporary storage
        otpStorage.remove(email);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}