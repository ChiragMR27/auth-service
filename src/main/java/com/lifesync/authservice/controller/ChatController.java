package com.lifesync.authservice.controller;

import com.lifesync.authservice.model.Message;
import com.lifesync.authservice.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final MessageRepository messageRepository;

    public ChatController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
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

    // THE FIX: New endpoint that returns a list of unique people who have messaged this user
    @GetMapping("/recent")
    public ResponseEntity<Set<String>> getRecentChats(@RequestParam String email) {
        List<Message> allMessages = messageRepository.findBySenderEmailOrReceiverEmail(email, email);
        Set<String> contacts = new HashSet<>();

        for (Message msg : allMessages) {
            if (!msg.getSenderEmail().equals(email)) {
                contacts.add(msg.getSenderEmail());
            }
            if (!msg.getReceiverEmail().equals(email)) {
                contacts.add(msg.getReceiverEmail());
            }
        }

        return ResponseEntity.ok(contacts);
    }
}