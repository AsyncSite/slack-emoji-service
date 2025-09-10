package com.asyncsite.slackemojiservice.emoji.application.service;

import com.asyncsite.slackemojiservice.emoji.application.port.in.GetEmojiPackUseCase;
import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import com.asyncsite.slackemojiservice.emoji.domain.port.out.LoadEmojiPackPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiPackService implements GetEmojiPackUseCase {
    
    private final LoadEmojiPackPort loadEmojiPackPort;
    
    @Override
    public List<EmojiPack> getAllPacks() {
        log.debug("Fetching all emoji packs");
        return loadEmojiPackPort.findAll();
    }
    
    @Override
    public Optional<EmojiPack> getPackById(String packId) {
        log.debug("Fetching emoji pack with ID: {}", packId);
        return loadEmojiPackPort.findById(packId);
    }
    
    @Override
    public List<EmojiPack> getPacksByCategory(String category) {
        log.debug("Fetching emoji packs by category: {}", category);
        return loadEmojiPackPort.findByCategory(category);
    }
    
    @Override
    public List<EmojiPack> getFeaturedPacks() {
        log.debug("Fetching featured emoji packs");
        return loadEmojiPackPort.findFeatured();
    }
    
    @Override
    public List<EmojiPack> searchPacks(String query) {
        log.debug("Searching emoji packs with query: {}", query);
        return loadEmojiPackPort.search(query);
    }
}