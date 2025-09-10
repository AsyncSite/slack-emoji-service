package com.asyncsite.slackemojiservice.emoji.domain.port.out;

import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;

public interface SaveEmojiPackPort {
    
    EmojiPack savePack(EmojiPack emojiPack);
    
    void deletePack(Long packId);
    
    void incrementDownloadCount(Long packId);
}