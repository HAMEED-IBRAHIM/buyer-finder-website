package com.example.buyerfinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public void sendEmail(String to, String subject, String body) {
        if (mailUsername == null || mailUsername.trim().isEmpty() || mailUsername.equalsIgnoreCase("your-email@gmail.com")) {
            logger.info("[DEMO MODE] Real SMTP credentials not configured. Simulated sending email to: {}, subject: {}", to, subject);
            return;
        }

        if (mailSender == null) {
            throw new RuntimeException("MailSender is not configured.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(mailUsername);

        try {
            mailSender.send(message);
            logger.info("Successfully sent email to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
