package com.example.buyerfinder.controller;

import com.example.buyerfinder.model.Buyer;
import com.example.buyerfinder.model.EmailRequest;
import com.example.buyerfinder.service.EmailService;
import com.example.buyerfinder.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class BuyerController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of(
                "name", "Demo User",
                "email", "demo@decorleads.com",
                "picture", "https://ui-avatars.com/api/?name=Demo+User&background=7c3aed&color=fff&size=64"
            ));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("picture", principal.getAttribute("picture"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Buyer>> searchBuyers(@RequestParam String query, @AuthenticationPrincipal OAuth2User principal) {
        List<Buyer> buyers = geminiService.findBuyers(query);
        return ResponseEntity.ok(buyers);
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request,
                                                         @AuthenticationPrincipal OAuth2User principal) {
        try {
            // Fire async; wait up to 8 seconds so Render doesn't timeout the request
            CompletableFuture<String> future = emailService.sendEmail(
                    request.getTo(), request.getSubject(), request.getBody());
            future.get(8, TimeUnit.SECONDS);
            return ResponseEntity.ok(Map.of("status", "Email sent successfully"));
        } catch (java.util.concurrent.TimeoutException te) {
            // Still accepted — email is still sending in background
            return ResponseEntity.ok(Map.of("status", "Email queued — it may take a moment to arrive"));
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send email: " + cause.getMessage()));
        }
    }
}
