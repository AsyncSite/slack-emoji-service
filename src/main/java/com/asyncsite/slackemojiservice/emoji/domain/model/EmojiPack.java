package com.asyncsite.slackemojiservice.emoji.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmojiPack {
    
    private String id;
    private String name;
    private String description;
    private String author;
    private String category;
    private String version;
    
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    @Builder.Default
    private List<String> preview = new ArrayList<>();
    
    @Builder.Default
    private List<Emoji> emojis = new ArrayList<>();
    
    private int emojiCount;
    private int downloadCount;
    private boolean featured;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public void incrementDownloadCount() {
        this.downloadCount++;
    }
    
    public void addEmoji(Emoji emoji) {
        this.emojis.add(emoji);
        this.emojiCount = this.emojis.size();
    }
    
    public void removeEmoji(Emoji emoji) {
        this.emojis.remove(emoji);
        this.emojiCount = this.emojis.size();
    }
    
    public void updateDetails(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}