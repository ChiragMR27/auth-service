package com.lifesync.authservice.repository;

import com.lifesync.authservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Quickly grabs messages sent from Person A to Person B
    List<Message> findBySenderEmailAndReceiverEmail(String senderEmail, String receiverEmail);
}