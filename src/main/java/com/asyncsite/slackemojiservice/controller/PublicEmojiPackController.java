package com.asyncsite.slackemojiservice.controller;

import com.asyncsite.slackemojiservice.emoji.application.port.in.GetEmojiPackUseCase;
import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public controller for emoji pack endpoints that don't require authentication
 * These endpoints are accessible through the Gateway at /api/public/slack-emoji/**
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public Emoji Pack API", description = "Public APIs for accessing emoji packs without authentication")
public class PublicEmojiPackController {

    private final GetEmojiPackUseCase getEmojiPackUseCase;

    /**
     * Get all emoji packs (public access)
     */
    @GetMapping("/v1/packs")
    @Operation(summary = "Get all emoji packs (public)")
    public ResponseEntity<List<EmojiPack>> getAllPacks() {
        log.info("Public access: Fetching all emoji packs");
        List<EmojiPack> packs = getEmojiPackUseCase.getAllPacks();
        return ResponseEntity.ok(packs);
    }

    /**
     * Get emoji pack by ID (public access)
     */
    @GetMapping("/v1/packs/{packId}")
    @Operation(summary = "Get emoji pack by ID (public)")
    public ResponseEntity<EmojiPack> getPackById(@PathVariable String packId) {
        log.info("Public access: Fetching emoji pack with ID: {}", packId);
        return getEmojiPackUseCase.getPackById(packId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get featured emoji packs (public access)
     */
    @GetMapping("/v1/packs/featured")
    @Operation(summary = "Get featured emoji packs (public)")
    public ResponseEntity<List<EmojiPack>> getFeaturedPacks() {
        log.info("Public access: Fetching featured emoji packs");
        List<EmojiPack> packs = getEmojiPackUseCase.getFeaturedPacks();
        return ResponseEntity.ok(packs);
    }

    /**
     * Get emoji packs by category (public access)
     */
    @GetMapping("/v1/packs/category/{category}")
    @Operation(summary = "Get emoji packs by category (public)")
    public ResponseEntity<List<EmojiPack>> getPacksByCategory(@PathVariable String category) {
        log.info("Public access: Fetching emoji packs for category: {}", category);
        List<EmojiPack> packs = getEmojiPackUseCase.getPacksByCategory(category);
        return ResponseEntity.ok(packs);
    }

    /**
     * Search emoji packs (public access)
     */
    @GetMapping("/v1/packs/search")
    @Operation(summary = "Search emoji packs (public)")
    public ResponseEntity<List<EmojiPack>> searchPacks(@RequestParam String q) {
        log.info("Public access: Searching emoji packs with query: {}", q);
        List<EmojiPack> packs = getEmojiPackUseCase.searchPacks(q);
        return ResponseEntity.ok(packs);
    }

    /**
     * Health check endpoint (public access)
     */
    @GetMapping({"/health", "/v1/health"})
    @Operation(summary = "Health check (public)")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Slack Emoji Service is healthy");
    }
}