package com.asyncsite.slackemojiservice.install.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackEmojiAdminClient {

    private final RestTemplate restTemplate;

    private static final String ADMIN_EMOJI_ADD = "https://slack.com/api/admin.emoji.add";
    private static final String ADMIN_EMOJI_ADD_ALIAS = "https://slack.com/api/admin.emoji.addAlias";

    public boolean addEmoji(String accessToken, String name, String imageUrl, String teamId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("name", name);
            form.add("url", imageUrl);
            if (teamId != null && !teamId.isBlank()) {
                form.add("team_id", teamId);
            }

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
            var resp = restTemplate.postForEntity(ADMIN_EMOJI_ADD, req, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.debug("Added emoji: {}", name);
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to add emoji {}: {}", name, e.getMessage());
        }
        return false;
    }

    public boolean addEmojiAlias(String accessToken, String alias, String targetName, String teamId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("alias_for", targetName);
            form.add("name", alias);
            if (teamId != null && !teamId.isBlank()) {
                form.add("team_id", teamId);
            }

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
            var resp = restTemplate.postForEntity(ADMIN_EMOJI_ADD_ALIAS, req, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.debug("Added alias {} for {}", alias, targetName);
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to add alias {} for {}: {}", alias, targetName, e.getMessage());
        }
        return false;
    }
}
