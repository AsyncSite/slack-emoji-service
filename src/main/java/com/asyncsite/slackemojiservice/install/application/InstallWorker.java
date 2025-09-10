package com.asyncsite.slackemojiservice.install.application;

import com.asyncsite.slackemojiservice.emoji.adapter.out.github.GitHubPackRepository;
import com.asyncsite.slackemojiservice.emoji.domain.model.Emoji;
import com.asyncsite.slackemojiservice.emoji.domain.model.EmojiPack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstallWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GitHubPackRepository packRepository;
    private final SlackEmojiAdminClient slackEmojiAdminClient;

    private static final String QUEUE_KEY = "install:queue";

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        Object jobIdObj = listOps.rightPop(QUEUE_KEY);
        if (jobIdObj == null) {
            return;
        }
        String jobId = jobIdObj.toString();
        String jobKey = "install:job:" + jobId;
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        Map<Object, Object> job = hash.entries(jobKey);
        if (job.isEmpty()) {
            return;
        }

        String packId = (String) job.get("packId");
        String sessionId = (String) job.get("sessionId");
        String sessionKey = "session:" + sessionId;
        String accessToken = (String) redisTemplate.opsForHash().get(sessionKey, "accessToken");
        if (accessToken == null) {
            hash.put(jobKey, "status", "failed");
            hash.put(jobKey, "completedAt", String.valueOf(System.currentTimeMillis()));
            return;
        }

        try {
            hash.put(jobKey, "status", "processing");

            EmojiPack pack = packRepository.findById(packId).orElse(null);
            if (pack == null) {
                hash.put(jobKey, "status", "failed");
                hash.put(jobKey, "completedAt", String.valueOf(System.currentTimeMillis()));
                return;
            }
            List<Emoji> emojis = pack.getEmojis();
            int total = emojis != null ? emojis.size() : 0;
            hash.put(jobKey, "total", total);

            int progress = 0;
            String teamId = (String) redisTemplate.opsForHash().get(sessionKey, "teamId");
            for (Emoji emoji : emojis) {
                boolean ok = slackEmojiAdminClient.addEmoji(accessToken, emoji.getName(), emoji.getImageUrl(), teamId);
                if (ok && emoji.getAliases() != null) {
                    for (String alias : emoji.getAliases()) {
                        slackEmojiAdminClient.addEmojiAlias(accessToken, alias, emoji.getName(), teamId);
                    }
                }
                progress++;
                hash.put(jobKey, "progress", progress);
            }

            hash.put(jobKey, "status", "completed");
            hash.put(jobKey, "completedAt", String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Install job {} failed", jobId, e);
            hash.put(jobKey, "status", "failed");
            hash.put(jobKey, "completedAt", String.valueOf(System.currentTimeMillis()));
        }
    }
}
