package com.asyncsite.slackemojiservice.emoji.domain.port.out;

import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import java.util.List;
import java.util.Optional;

public interface LoadEmojiPackPort {
    
    List<EmojiPack> findAll();
    
    Optional<EmojiPack> findById(String packId);
    
    List<EmojiPack> findByCategory(String category);
    
    List<EmojiPack> findFeatured();
    
    List<EmojiPack> search(String query);
}