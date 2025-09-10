package com.asyncsite.slackemojiservice.emoji.application.port.in;

import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import java.util.List;
import java.util.Optional;

public interface GetEmojiPackUseCase {
    
    List<EmojiPack> getAllPacks();
    
    Optional<EmojiPack> getPackById(String packId);
    
    List<EmojiPack> getPacksByCategory(String category);
    
    List<EmojiPack> getFeaturedPacks();
    
    List<EmojiPack> searchPacks(String query);
}