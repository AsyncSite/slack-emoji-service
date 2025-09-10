package com.asyncsite.slackemojiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (via Gateway /api/public/slack-emoji/** routes)
                .requestMatchers("/api/public/**").permitAll()
                // Public endpoints (direct access)
                .requestMatchers(
                    "/api/v1/health",
                    "/api/v1/packs/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                // Slack OAuth endpoints (moved to public path)
                .requestMatchers(
                    "/api/public/v1/slack/auth",
                    "/api/public/v1/slack/callback"
                ).permitAll()
                // Install endpoints (temporary public for testing)
                .requestMatchers("/api/v1/install/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}