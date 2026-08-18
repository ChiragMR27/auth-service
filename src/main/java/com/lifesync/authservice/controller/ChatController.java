package com.lifesync.authservice.controller;

import com.lifesync.authservice.model.AppUser;
import com.lifesync.authservice.model.Message;
import com.lifesync.authservice.repository.MessageRepository;
import com.lifesync.authservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository; // THE FIX: Added UserRepository!

    public ChatController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Message>> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        List<Message> sent = messageRepository.findBySenderEmailAndReceiverEmail(user1, user2);
        List<Message> received = messageRepository.findBySenderEmailAndReceiverEmail(user2, user1);
        
        List<Message> history = Stream.concat(sent.stream(), received.stream())
                .sorted(Comparator.comparing(Message::getId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    // THE FIX: Now returns a list of objects containing BOTH the email and the username!
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, String>>> getRecentChats(@RequestParam String email) {
        List<Message> allMessages = messageRepository.findBySenderEmailOrReceiverEmail(email, email);
        Set<String> uniqueEmails = new HashSet<>();

        for (Message msg : allMessages) {
            if (!msg.getSenderEmail().equals(email)) {
                uniqueEmails.add(msg.getSenderEmail());
            }
            if (!msg.getReceiverEmail().equals(email)) {
                uniqueEmails.add(msg.getReceiverEmail());
            }
        }

        List<Map<String, String>> recentChats = new ArrayList<>();
        for (String contactEmail : uniqueEmails) {
            Map<String, String> contactInfo = new HashMap<>();
            contactInfo.put("email", contactEmail);
            
            // Database Lookup: Get the username!
            Optional<AppUser> userOpt = userRepository.findByEmail(contactEmail);
            if (userOpt.isPresent()) {
                contactInfo.put("username", userOpt.get().getUsername());
            } else {
                contactInfo.put("username", contactEmail.split("@")[0]); // Fallback
            }
            recentChats.add(contactInfo);
        }

        return ResponseEntity.ok(recentChats);
    }
}