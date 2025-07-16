package org.choon.careerbee.common.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeInitEvent;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeUpdateEvent;
import org.choon.careerbee.common.pubsub.dto.ResumeExtractedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String RESUME_EXTRACT_CHANNEL = "resume.extract.complete";
    private static final String INIT_ADVANCED_RESUME_CHANNEL = "advanced.resume.init.complete";
    private static final String UPDATE_ADVANCED_RESUME_CHANNEL = "advanced.resume.update.complete";

    public void publishResumeExtractedEvent(ResumeExtractedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(RESUME_EXTRACT_CHANNEL, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishAdvancedResumeInitEvent(AdvancedResumeInitEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(INIT_ADVANCED_RESUME_CHANNEL, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishAdvancedResumeUpdateEvent(AdvancedResumeUpdateEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(UPDATE_ADVANCED_RESUME_CHANNEL, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }
}
