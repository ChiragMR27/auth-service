package com.lifesync.authservice.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    // Note: Until you add a custom domain to Resend, you MUST use this testing 'from' address
                    .from("LifeSync <onboarding@resend.dev>") 
                    .to(toEmail)
                    .subject("LifeSync - Your Verification Code")
                    .html("<div style='font-family: Arial; padding: 20px; color: #333;'>" +
                          "<h2>Welcome to LifeSync!</h2>" +
                          "<p>Your 4-digit registration OTP is:</p>" +
                          "<h1 style='color: #00e5ff; font-size: 36px; letter-spacing: 5px;'>" + otp + "</h1>" +
                          "</div>")
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("Background Thread: OTP successfully sent to " + toEmail + " with ID: " + data.getId());

        } catch (ResendException e) {
            System.err.println("Background Thread: Failed to send OTP to " + toEmail);
            e.printStackTrace();
        }
    }
}