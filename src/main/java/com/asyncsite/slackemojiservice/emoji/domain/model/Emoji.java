package com.asyncsite.slackemojiservice.emoji.domain.model;

import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "name")
public class Emoji {
    
    private Long id;
    private String name;
    private String imageUrl;
    private List<String> aliases;
    private Long packId;
    
    public boolean isValidName() {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Slack emoji name rules: lowercase letters, numbers, underscore, dash
        // Must start with a letter, max 50 characters
        return name.matches("^[a-z][a-z0-9_-]{0,49}$");
    }
    
    public boolean isValidImageUrl() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        // Check for supported image formats
        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.endsWith(".png") || 
               lowerUrl.endsWith(".jpg") || 
               lowerUrl.endsWith(".jpeg") || 
               lowerUrl.endsWith(".gif");
    }
}