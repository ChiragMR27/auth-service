package com.lifesync.authservice.controller;

import com.lifesync.authservice.model.Message;
import com.lifesync.authservice.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/chat")
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
        // Fetch both sides of the conversation
        List<Message> sent = messageRepository.findBySenderEmailAndReceiverEmail(user1, user2);
        List<Message> received = messageRepository.findBySenderEmailAndReceiverEmail(user2, user1);
        
        // Combine them and sort chronologically by ID
        List<Message> history = Stream.concat(sent.stream(), received.stream())
                .sorted(Comparator.comparing(Message::getId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}