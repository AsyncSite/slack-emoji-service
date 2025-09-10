package com.asyncsite.slackemojiservice.emoji.adapter.out.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPackDetails {
    private String id;
    private String name;
    private String description;
    private String version;
    private List<EmojiInfo> emojis;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmojiInfo {
        private String name;
        private List<String> aliases;
        private String imageUrl;
    }
}