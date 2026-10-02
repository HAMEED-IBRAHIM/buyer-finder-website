package com.example.buyerfinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:465}")
    private int mailPort;

    @Async
    public CompletableFuture<String> sendEmail(String to, String subject, String body) {
        // Demo mode — no real credentials configured
        if (mailUsername == null || mailUsername.trim().isEmpty()
                || mailUsername.equalsIgnoreCase("your-email@gmail.com")) {
            logger.info("[DEMO MODE] Simulated email to: {}", to);
            return CompletableFuture.completedFuture("demo");
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailHost);
            props.put("mail.smtp.port", String.valueOf(mailPort));
            props.put("mail.smtp.auth", "true");

            if (mailPort == 465) {
                // SSL mode
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.trust", mailHost);
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
            } else {
                // STARTTLS mode (port 587)
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            }

            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUsername, mailPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture("sent");

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(
                    new RuntimeException("Failed to send email: " + e.getMessage(), e));
        }
    }
}
