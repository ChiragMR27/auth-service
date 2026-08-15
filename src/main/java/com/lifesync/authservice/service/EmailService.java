package com.lifesync.authservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // The @Async annotation forces this method to run on a separate background thread
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("LifeSync - Your Verification Code");
            message.setText("Welcome to LifeSync! Your 4-digit registration OTP is: " + otp);
            
            mailSender.send(message);
            System.out.println("Background Thread: OTP successfully sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Background Thread: Failed to send OTP to " + toEmail);
            e.printStackTrace();
        }
    }
}