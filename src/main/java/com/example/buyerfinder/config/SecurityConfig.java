package com.example.buyerfinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public static pages and API
                .requestMatchers(
                    "/", "/index.html", "/login.html",
                    "/css/**", "/js/**",
                    "/error", "/oauth2/**", "/login**",
                    "/api/user", "/api/search", "/api/email"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login.html")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login.html?error=oauth")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login.html")
                .permitAll()
            );
        return http.build();
    }
}
