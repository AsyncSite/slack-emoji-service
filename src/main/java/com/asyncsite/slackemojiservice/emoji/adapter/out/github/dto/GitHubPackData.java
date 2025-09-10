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
public class GitHubPackData {
    private List<PackInfo> packs;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackInfo {
        private String id;
        private String name;
        private String description;
        private String category;
        private int emojiCount;
        private String author;
        private List<String> tags;
        private List<String> preview;
        private boolean featured;
        private String createdAt;
        private String updatedAt;
    }
}