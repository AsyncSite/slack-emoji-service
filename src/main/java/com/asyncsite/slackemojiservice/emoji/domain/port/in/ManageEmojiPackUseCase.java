package com.asyncsite.slackemojiservice.emoji.domain.port.in;

import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import java.util.List;
import java.util.Optional;

public interface ManageEmojiPackUseCase {
    
    List<EmojiPack> getAllPacks();
    
    List<EmojiPack> getFeaturedPacks();
    
    List<EmojiPack> getPacksByCategory(String category);
    
    Optional<EmojiPack> getPackById(Long packId);
    
    EmojiPack createPack(CreatePackCommand command);
    
    EmojiPack updatePack(Long packId, UpdatePackCommand command);
    
    void deletePack(Long packId);
    
    void incrementDownloadCount(Long packId);
    
    record CreatePackCommand(
        String name,
        String description,
        String author,
        String category
    ) {}
    
    record UpdatePackCommand(
        String name,
        String description,
        String category,
        Boolean featured
    ) {}
}