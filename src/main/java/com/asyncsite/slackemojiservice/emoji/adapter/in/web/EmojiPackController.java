package com.asyncsite.slackemojiservice.emoji.adapter.in.web;

import com.asyncsite.slackemojiservice.emoji.application.port.in.GetEmojiPackUseCase;
import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/packs")
@RequiredArgsConstructor
@Tag(name = "Emoji Pack Management", description = "APIs for managing emoji packs")
public class EmojiPackController {
    
    private final GetEmojiPackUseCase getEmojiPackUseCase;
    
    @GetMapping
    @Operation(summary = "Get all emoji packs")
    public ResponseEntity<List<EmojiPack>> getAllPacks() {
        log.info("Fetching all emoji packs");
        List<EmojiPack> packs = getEmojiPackUseCase.getAllPacks();
        return ResponseEntity.ok(packs);
    }
    
    @GetMapping("/{packId}")
    @Operation(summary = "Get emoji pack by ID")
    public ResponseEntity<EmojiPack> getPackById(@PathVariable String packId) {
        log.info("Fetching emoji pack with ID: {}", packId);
        return getEmojiPackUseCase.getPackById(packId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured emoji packs")
    public ResponseEntity<List<EmojiPack>> getFeaturedPacks() {
        log.info("Fetching featured emoji packs");
        List<EmojiPack> packs = getEmojiPackUseCase.getFeaturedPacks();
        return ResponseEntity.ok(packs);
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get emoji packs by category")
    public ResponseEntity<List<EmojiPack>> getPacksByCategory(@PathVariable String category) {
        log.info("Fetching emoji packs for category: {}", category);
        List<EmojiPack> packs = getEmojiPackUseCase.getPacksByCategory(category);
        return ResponseEntity.ok(packs);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search emoji packs")
    public ResponseEntity<List<EmojiPack>> searchPacks(@RequestParam String q) {
        log.info("Searching emoji packs with query: {}", q);
        List<EmojiPack> packs = getEmojiPackUseCase.searchPacks(q);
        return ResponseEntity.ok(packs);
    }
}