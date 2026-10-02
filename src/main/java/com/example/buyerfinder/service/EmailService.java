package com.example.buyerfinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Async
    public CompletableFuture<String> sendEmail(String to, String subject, String body) {
        // Demo mode — no real credentials configured
        if (mailUsername == null || mailUsername.trim().isEmpty()
                || mailUsername.equalsIgnoreCase("your-email@gmail.com")) {
            logger.info("[DEMO MODE] Simulated email to: {}", to);
            return CompletableFuture.completedFuture("demo");
        }

        if (mailSender == null) {
            return CompletableFuture.failedFuture(
                    new RuntimeException("MailSender is not configured."));
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(mailUsername);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture("sent");
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(
                    new RuntimeException("Failed to send email: " + e.getMessage(), e));
        }
    }
}
