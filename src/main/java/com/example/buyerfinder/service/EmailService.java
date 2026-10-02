package com.example.buyerfinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Sends emails via Resend.com HTTP API (no SMTP — works on all cloud hosts including Render).
 * Fallback to demo mode if RESEND_API_KEY is not set.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Async
    public CompletableFuture<String> sendEmail(String to, String subject, String body) {

        // ── Primary: Resend HTTP API (works on Render) ──────────────────────
        if (resendApiKey != null && !resendApiKey.trim().isEmpty()
                && !resendApiKey.equalsIgnoreCase("your-resend-api-key")) {
            return sendViaResend(to, subject, body);
        }

        // ── Demo mode — no credentials configured ───────────────────────────
        logger.info("[DEMO MODE] No Resend API key found. Simulating send to: {}", to);
        return CompletableFuture.completedFuture("demo");
    }

    private CompletableFuture<String> sendViaResend(String to, String subject, String body) {
        try {
            RestTemplate rest = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            // Sender: use verified domain email if set, else Resend's free test address
            String fromAddress = (mailUsername != null && !mailUsername.isEmpty()
                    && !mailUsername.equalsIgnoreCase("your-email@gmail.com"))
                    ? "DecorLeads <" + mailUsername + ">"
                    : "DecorLeads <onboarding@resend.dev>";

            Map<String, Object> payload = new HashMap<>();
            payload.put("from", fromAddress);
            payload.put("to", List.of(to));
            payload.put("subject", subject);
            payload.put("text", body);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = rest.postForEntity(RESEND_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Email sent via Resend API to: {}", to);
                return CompletableFuture.completedFuture("sent");
            } else {
                String msg = "Resend API error: " + response.getStatusCode() + " — " + response.getBody();
                logger.error(msg);
                return CompletableFuture.failedFuture(new RuntimeException(msg));
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String msg = "Resend rejected request: " + e.getStatusCode() + " — " + e.getResponseBodyAsString();
            logger.error(msg);
            return CompletableFuture.failedFuture(new RuntimeException(msg));
        } catch (Exception e) {
            logger.error("Failed to send via Resend: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(
                    new RuntimeException("Failed to send email: " + e.getMessage(), e));
        }
    }
}
