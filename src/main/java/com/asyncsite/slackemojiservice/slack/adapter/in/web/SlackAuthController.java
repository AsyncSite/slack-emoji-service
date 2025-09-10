package com.asyncsite.slackemojiservice.slack.adapter.in.web;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/public/v1/slack")
@RequiredArgsConstructor
@Tag(name = "Slack Authentication", description = "Slack OAuth flow endpoints")
public class SlackAuthController {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${SLACK_CLIENT_ID:9494654010486.9498856782788}")
    private String clientId;

    @Value("${SLACK_REDIRECT_URI:http://localhost:8080/api/public/slack-emoji/v1/slack/callback}")
    private String redirectUri;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final String SLACK_AUTH_URL = "https://slack.com/oauth/v2/authorize";
    // Keep scopes minimal; include admin.emoji:write if enterprise is targeted
    private static final String SCOPES = "emoji:read,emoji:write,team:read,admin.emoji:write";

    private static final Duration STATE_TTL = Duration.ofMinutes(5);
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    @GetMapping("/auth")
    @Operation(summary = "Initiate Slack OAuth flow")
    public void initiateSlackAuth(
            @RequestParam(required = false) String packId,
            HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();

        // Store state and packId in Redis for CSRF protection and pack tracking
        String stateKey = buildStateKey(state);
        if (packId != null && !packId.isBlank()) {
            redisTemplate.opsForHash().put(stateKey, "packId", packId);
        }
        redisTemplate.expire(stateKey, STATE_TTL);

        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScopes = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);
        String authUrl = String.format("%s?client_id=%s&scope=%s&redirect_uri=%s&state=%s",
            SLACK_AUTH_URL,
            clientId,
            encodedScopes,
            encodedRedirectUri,
            state
        );

        log.info("Redirecting to Slack OAuth for packId: {}, URL: {}", packId, authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    @Operation(summary = "Handle Slack OAuth callback")
    public void handleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse httpResponse) throws IOException {

        if (error != null) {
            log.error("Slack OAuth error: {}", error);
            httpResponse.sendRedirect(frontendUrl + "/oauth/callback?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
            return;
        }

        if (state == null || state.isBlank()) {
            log.warn("Missing OAuth state");
            httpResponse.sendRedirect(frontendUrl + "/oauth/callback?error=missing_state");
            return;
        }

        String stateKey = buildStateKey(state);
        Object packIdObj = redisTemplate.opsForHash().get(stateKey, "packId");
        String packId = packIdObj instanceof String ? (String) packIdObj : null;
        if (packId == null) {
            log.warn("State not found or expired: {}", state);
            httpResponse.sendRedirect(frontendUrl + "/oauth/callback?error=invalid_state");
            return;
        }

        log.info("Received OAuth callback with code for pack {}", packId);

        String sessionId = UUID.randomUUID().toString();
        String sessionKey = buildSessionKey(sessionId);
        // Mock token/session for now (real exchange is TODO)
        redisTemplate.opsForHash().put(sessionKey, "accessToken", "mock-token");
        redisTemplate.opsForHash().put(sessionKey, "teamId", "T-MOCK");
        redisTemplate.opsForHash().put(sessionKey, "teamName", "Mock Team");
        redisTemplate.opsForHash().put(sessionKey, "createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.expire(sessionKey, SESSION_TTL);

        // Cleanup state
        redisTemplate.delete(stateKey);

        String redirectUrl = String.format("%s/oauth/callback?sessionId=%s&packId=%s",
                frontendUrl,
                URLEncoder.encode(sessionId, StandardCharsets.UTF_8),
                URLEncoder.encode(packId, StandardCharsets.UTF_8));
        httpResponse.sendRedirect(redirectUrl);
    }

    private String buildStateKey(String state) {
        return "oauth:state:" + state;
    }

    private String buildSessionKey(String sessionId) {
        return "session:" + sessionId;
    }
}