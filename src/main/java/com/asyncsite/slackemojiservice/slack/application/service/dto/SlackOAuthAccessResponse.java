package com.asyncsite.slackemojiservice.slack.application.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SlackOAuthAccessResponse {
    private Boolean ok;
    private String error;

    // Bot token (xoxb-...), not suitable for admin.* endpoints
    @JsonProperty("access_token")
    private String accessToken;

    // User token container
    @JsonProperty("authed_user")
    private AuthedUser authedUser;

    private Team team;

    @Data
    public static class Team {
        private String id;
        private String name;
    }

    @Data
    public static class AuthedUser {
        @JsonProperty("id")
        private String userId;

        // User token (xoxp-...) required for admin.emoji:write
        @JsonProperty("access_token")
        private String accessToken;
    }
}
