package com.asyncsite.slackemojiservice.emoji.adapter.out.github;

import com.asyncsite.slackemojiservice.emoji.adapter.out.github.dto.GitHubPackData;
import com.asyncsite.slackemojiservice.emoji.adapter.out.github.dto.GitHubPackDetails;
import com.asyncsite.slackemojiservice.emoji.domain.model.Emoji;
import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import com.asyncsite.slackemojiservice.emoji.domain.port.out.LoadEmojiPackPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubPackRepository implements LoadEmojiPackPort {
    
    private static final String REPO_BASE = "https://raw.githubusercontent.com/AsyncSite/slack-emoji-packs/main";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    // @Cacheable(value = "emojiPacks", unless = "#result.isEmpty()")  // TODO: Fix serialization issue
    public List<EmojiPack> findAll() {
        try {
            String url = REPO_BASE + "/packs.json";
            log.debug("Fetching packs from GitHub: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            GitHubPackData packData = objectMapper.readValue(response, GitHubPackData.class);
            
            return packData.getPacks().stream()
                .map(this::convertToEmojiPack)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch packs from GitHub", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    // @Cacheable(value = "emojiPack", key = "#packId", unless = "#result == null")  // TODO: Fix serialization issue
    public Optional<EmojiPack> findById(String packId) {
        try {
            // First get basic info from packs.json
            List<EmojiPack> allPacks = findAll();
            Optional<EmojiPack> basicPack = allPacks.stream()
                .filter(p -> p.getId().equals(packId))
                .findFirst();
            
            if (basicPack.isEmpty()) {
                return Optional.empty();
            }
            
            // Then get detailed info
            String detailUrl = REPO_BASE + "/packs/" + packId + "/pack.json";
            log.debug("Fetching pack details from GitHub: {}", detailUrl);
            
            String response = restTemplate.getForObject(detailUrl, String.class);
            GitHubPackDetails details = objectMapper.readValue(response, GitHubPackDetails.class);
            
            // Merge basic info with details
            EmojiPack pack = basicPack.get();
            pack.setVersion(details.getVersion());
            pack.setEmojis(convertEmojis(details.getEmojis(), packId));
            
            return Optional.of(pack);
        } catch (Exception e) {
            log.error("Failed to fetch pack {} from GitHub", packId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<EmojiPack> findByCategory(String category) {
        return findAll().stream()
            .filter(p -> category.equalsIgnoreCase(p.getCategory()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EmojiPack> findFeatured() {
        return findAll().stream()
            .filter(EmojiPack::isFeatured)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EmojiPack> search(String query) {
        String lowerQuery = query.toLowerCase();
        return findAll().stream()
            .filter(p -> 
                p.getName().toLowerCase().contains(lowerQuery) ||
                p.getDescription().toLowerCase().contains(lowerQuery) ||
                p.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    private EmojiPack convertToEmojiPack(GitHubPackData.PackInfo info) {
        return EmojiPack.builder()
            .id(info.getId())
            .name(info.getName())
            .description(info.getDescription())
            .author(info.getAuthor())
            .category(info.getCategory())
            .emojiCount(info.getEmojiCount())
            .tags(info.getTags())
            .preview(info.getPreview())
            .featured(info.isFeatured())
            .createdAt(parseDateTime(info.getCreatedAt()))
            .updatedAt(parseDateTime(info.getUpdatedAt()))
            .build();
    }
    
    private LocalDateTime parseDateTime(String dateStr) {
        // GitHub date format is YYYY-MM-DD, need to add time component
        if (dateStr != null && dateStr.length() == 10) {
            return LocalDateTime.parse(dateStr + "T00:00:00");
        }
        return LocalDateTime.parse(dateStr);
    }
    
    private List<Emoji> convertEmojis(List<GitHubPackDetails.EmojiInfo> emojiInfos, String packId) {
        return emojiInfos.stream()
            .map(info -> Emoji.builder()
                .name(info.getName())
                .aliases(info.getAliases())
                .imageUrl(buildEmojiImageUrl(packId, info.getName()))
                .build())
            .collect(Collectors.toList());
    }
    
    private String buildEmojiImageUrl(String packId, String emojiName) {
        // GitHub raw URL for emoji images
        return REPO_BASE + "/images/" + packId + "/" + emojiName + ".png";
    }
}