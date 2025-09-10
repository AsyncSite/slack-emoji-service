package com.asyncsite.slackemojiservice.slack.application.service;

import com.asyncsite.slackemojiservice.slack.application.service.dto.SlackOAuthAccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackOAuthService {

    private final RestTemplate restTemplate;

    @Value("${SLACK_CLIENT_ID:}")
    private String clientId;

    @Value("${SLACK_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${SLACK_REDIRECT_URI:http://localhost:8080/api/public/slack-emoji/v1/slack/callback}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://slack.com/api/oauth.v2.access";

    public SlackOAuthAccessResponse exchangeCodeForAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        SlackOAuthAccessResponse response = restTemplate.postForObject(
                TOKEN_URL, request, SlackOAuthAccessResponse.class);

        log.debug("Slack OAuth exchange response: {}", response);
        return response;
    }
}
