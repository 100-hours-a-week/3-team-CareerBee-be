package org.choon.careerbee.common.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeInitEvent;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeUpdateEvent;
import org.choon.careerbee.common.pubsub.dto.AiErrorEvent;
import org.choon.careerbee.common.pubsub.dto.FeedbackEvent;
import org.choon.careerbee.common.pubsub.dto.ResumeExtractedEvent;
import org.choon.careerbee.common.pubsub.enums.Channel;
import org.choon.careerbee.domain.competition.dto.event.PointEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publishResumeExtractedEvent(ResumeExtractedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.RESUME_EXTRACTED.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishAdvancedResumeInitEvent(AdvancedResumeInitEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.ADVANCED_RESUME_INIT.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishAdvancedResumeUpdateEvent(AdvancedResumeUpdateEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.ADVANCED_RESUME_UPDATE.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishInterviewProblemFeedbackEvent(FeedbackEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.PROBLEM_FEEDBACK.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishAiErrorEvent(AiErrorEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.AI_ERROR_CHANNEL.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }

    public void publishPointEvent(PointEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(Channel.COMPETITION_POINT.getValue(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 직렬화 실패", e);
        }
    }
}
