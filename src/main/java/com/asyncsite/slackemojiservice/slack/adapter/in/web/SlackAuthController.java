package com.asyncsite.slackemojiservice.slack.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/public/v1/slack")
@RequiredArgsConstructor
@Tag(name = "Slack Authentication", description = "Slack OAuth flow endpoints")
public class SlackAuthController {
    
    @Value("${SLACK_CLIENT_ID:9494654010486.9498856782788}")
    private String clientId;
    
    @Value("${SLACK_REDIRECT_URI:http://localhost:8080/api/public/slack-emoji/v1/slack/callback}")
    private String redirectUri;
    
    private static final String SLACK_AUTH_URL = "https://slack.com/oauth/v2/authorize";
    private static final String SCOPES = "emoji:read,emoji:write,team:read";
    
    @GetMapping("/auth")
    @Operation(summary = "Initiate Slack OAuth flow")
    public void initiateSlackAuth(
            @RequestParam(required = false) String packId,
            HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        
        // TODO: Store state and packId in Redis for CSRF protection and pack tracking
        
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String authUrl = String.format("%s?client_id=%s&scope=%s&redirect_uri=%s&state=%s",
            SLACK_AUTH_URL,
            clientId,
            SCOPES,
            encodedRedirectUri,
            state
        );
        
        log.info("Redirecting to Slack OAuth for packId: {}, URL: {}", packId, authUrl);
        response.sendRedirect(authUrl);
    }
    
    @GetMapping("/callback")
    @Operation(summary = "Handle Slack OAuth callback")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {
        
        if (error != null) {
            log.error("Slack OAuth error: {}", error);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", error);
            errorResponse.put("message", "Slack authorization failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        log.info("Received OAuth callback with code: {}", code);
        
        // TODO: Validate state for CSRF protection
        // TODO: Exchange code for access token
        // TODO: Store access token in Redis session
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Authentication successful");
        response.put("sessionId", UUID.randomUUID().toString()); // Temporary session ID
        
        return ResponseEntity.ok(response);
    }
}