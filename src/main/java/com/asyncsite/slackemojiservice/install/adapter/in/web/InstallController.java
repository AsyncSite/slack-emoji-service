package com.asyncsite.slackemojiservice.install.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/install")
@RequiredArgsConstructor
@Tag(name = "Installation", description = "Emoji pack installation endpoints")
public class InstallController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @PostMapping("/{packId}")
    @Operation(summary = "Start emoji pack installation")
    public ResponseEntity<Map<String, Object>> installPack(
            @PathVariable String packId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        
        log.info("Starting installation for pack: {}", packId);
        
        // Generate job ID
        String jobId = UUID.randomUUID().toString();
        
        // Create installation job in Redis
        String jobKey = "install:job:" + jobId;
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("packId", packId);
        jobData.put("status", "pending");
        jobData.put("progress", 0);
        jobData.put("total", 0);
        jobData.put("startedAt", System.currentTimeMillis());
        
        // Store job data in Redis with 1 hour TTL
        redisTemplate.opsForHash().putAll(jobKey, jobData);
        redisTemplate.expire(jobKey, 1, TimeUnit.HOURS);
        
        // TODO: Add job to processing queue
        // TODO: Start worker to process installation
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", "started");
        response.put("message", "Installation started");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{jobId}")
    @Operation(summary = "Get installation job status")
    public ResponseEntity<Map<String, Object>> getInstallStatus(@PathVariable String jobId) {
        
        String jobKey = "install:job:" + jobId;
        Map<Object, Object> jobData = redisTemplate.opsForHash().entries(jobKey);
        
        if (jobData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", jobData.get("status"));
        response.put("progress", jobData.get("progress"));
        response.put("total", jobData.get("total"));
        
        // Add completion data if finished
        if ("completed".equals(jobData.get("status")) || "failed".equals(jobData.get("status"))) {
            response.put("completedAt", jobData.get("completedAt"));
            if (jobData.containsKey("errors")) {
                response.put("errors", jobData.get("errors"));
            }
        }
        
        return ResponseEntity.ok(response);
    }
}