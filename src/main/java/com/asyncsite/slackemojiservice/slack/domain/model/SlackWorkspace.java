package com.asyncsite.slackemojiservice.slack.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SlackWorkspace {
    
    private Long id;
    private Long userId;
    private String teamId;
    private String teamName;
    private String accessToken;
    private String botUserId;
    private LocalDateTime connectedAt;
    private LocalDateTime lastUsedAt;
    
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public void updateAccessToken(String newToken) {
        this.accessToken = newToken;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public boolean isTokenExpired() {
        // Slack tokens don't expire unless revoked, but we can add business logic here
        // For example, consider a token "stale" after 90 days of no use
        if (lastUsedAt == null) {
            return false;
        }
        return lastUsedAt.isBefore(LocalDateTime.now().minusDays(90));
    }
}