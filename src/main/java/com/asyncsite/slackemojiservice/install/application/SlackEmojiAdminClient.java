package com.asyncsite.slackemojiservice.install.application;

import com.asyncsite.slackemojiservice.install.application.dto.SlackBasicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackEmojiAdminClient {

    private final RestTemplate restTemplate;

    private static final String ADMIN_EMOJI_ADD = "https://slack.com/api/admin.emoji.add";
    private static final String ADMIN_EMOJI_ADD_ALIAS = "https://slack.com/api/admin.emoji.addAlias";

    public boolean addEmoji(String accessToken, String name, String imageUrl, String teamId) {
        HttpHeaders headers = createHeaders(accessToken);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("url", imageUrl);
        if (teamId != null && !teamId.isBlank()) {
            form.add("team_id", teamId);
        }
        SlackBasicResponse resp = postWithRetry(ADMIN_EMOJI_ADD, headers, form);
        if (resp == null) return false;
        if (Boolean.TRUE.equals(resp.getOk())) return true;
        // Treat existing name as success
        return isNonFatalExistenceError(resp.getError());
    }

    public boolean addEmojiAlias(String accessToken, String alias, String targetName, String teamId) {
        HttpHeaders headers = createHeaders(accessToken);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("alias_for", targetName);
        form.add("name", alias);
        if (teamId != null && !teamId.isBlank()) {
            form.add("team_id", teamId);
        }
        SlackBasicResponse resp = postWithRetry(ADMIN_EMOJI_ADD_ALIAS, headers, form);
        if (resp == null) return false;
        if (Boolean.TRUE.equals(resp.getOk())) return true;
        return isNonFatalExistenceError(resp.getError());
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private boolean isNonFatalExistenceError(String error) {
        if (error == null) return false;
        return switch (error) {
            case "name_taken", "already_exists", "emoji_name_taken", "duplicate" -> true;
            default -> false;
        };
    }

    private SlackBasicResponse postWithRetry(String url,
                                             HttpHeaders headers,
                                             MultiValueMap<String, String> form) {
        int maxAttempts = 5;
        long backoffMs = 500L;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
                ResponseEntity<SlackBasicResponse> resp = restTemplate.postForEntity(url, req, SlackBasicResponse.class);
                if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                    SlackBasicResponse body = resp.getBody();
                    if (Boolean.TRUE.equals(body.getOk())) {
                        return body;
                    }
                    // Retry on rate limit or transient errors
                    if ("ratelimited".equals(body.getError()) || "internal_error".equals(body.getError())) {
                        sleep(backoffMs);
                        backoffMs = Math.min(backoffMs * 2, 8000L);
                        continue;
                    }
                    return body;
                }
                // Non-2xx
                if (shouldRetryStatus(HttpStatus.valueOf(resp.getStatusCode().value()))) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 8000L);
                    continue;
                }
                return null;
            } catch (HttpStatusCodeException ex) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                if (shouldRetryStatus(status)) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 8000L);
                    continue;
                }
                log.warn("Slack API call failed: status={}, body={}", status.value(), ex.getResponseBodyAsString());
                return null;
            } catch (Exception e) {
                log.warn("Slack API call error: {}", e.getMessage());
                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 8000L);
            }
        }
        return null;
    }

    private boolean shouldRetryStatus(HttpStatus status) {
        return status == HttpStatus.TOO_MANY_REQUESTS || status.is5xxServerError();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
