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
            // Demo mode: return a guest user so UI enables search
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
        // Allow search in demo mode too
        List<Buyer> buyers = geminiService.findBuyers(query);
        return ResponseEntity.ok(buyers);
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request, @AuthenticationPrincipal OAuth2User principal) {
        try {
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
            return ResponseEntity.ok(Map.of("status", "Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }
}
