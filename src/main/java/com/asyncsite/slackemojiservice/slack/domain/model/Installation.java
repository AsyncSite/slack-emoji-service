package com.asyncsite.slackemojiservice.slack.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Installation {
    
    private Long id;
    private Long userId;
    private String workspaceId;
    private Long packId;
    private InstallationStatus status;
    private int totalEmojis;
    private int installedCount;
    private int failedCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    public static Installation start(Long userId, String workspaceId, Long packId, int totalEmojis) {
        return Installation.builder()
                .userId(userId)
                .workspaceId(workspaceId)
                .packId(packId)
                .status(InstallationStatus.IN_PROGRESS)
                .totalEmojis(totalEmojis)
                .installedCount(0)
                .failedCount(0)
                .startedAt(LocalDateTime.now())
                .build();
    }
    
    public void markEmojiInstalled() {
        this.installedCount++;
        updateProgress();
    }
    
    public void markEmojiFailed() {
        this.failedCount++;
        updateProgress();
    }
    
    private void updateProgress() {
        if (installedCount + failedCount >= totalEmojis) {
            if (failedCount == 0) {
                this.status = InstallationStatus.COMPLETED;
            } else if (installedCount == 0) {
                this.status = InstallationStatus.FAILED;
            } else {
                this.status = InstallationStatus.PARTIAL;
            }
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public void fail(String errorMessage) {
        this.status = InstallationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    public int getProgressPercentage() {
        if (totalEmojis == 0) return 100;
        return (int) ((installedCount + failedCount) * 100.0 / totalEmojis);
    }
}