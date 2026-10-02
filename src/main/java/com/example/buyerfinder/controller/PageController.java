package com.example.buyerfinder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // Redirect /login to /login.html so the custom login page shows up
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }
}
