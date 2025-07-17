package org.choon.careerbee.common.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeInitEvent;
import org.choon.careerbee.common.pubsub.dto.AdvancedResumeUpdateEvent;
import org.choon.careerbee.common.pubsub.dto.AiErrorEvent;
import org.choon.careerbee.common.pubsub.dto.FeedbackEvent;
import org.choon.careerbee.common.pubsub.dto.ResumeExtractedEvent;
import org.choon.careerbee.common.pubsub.enums.Channel;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            log.info("[RedisSubscriber] 채널 수신: {}", channel);

            switch (Channel.from(channel)) {
                case Channel.RESUME_EXTRACTED -> {
                    log.info("이력서 정보 추출 요청 성공 및 SSE 송신 시작");
                    ResumeExtractedEvent event = objectMapper.readValue(
                        json, ResumeExtractedEvent.class
                    );
                    sseService.pushResumeExtracted(event.memberId(), event.result());
                }

                case Channel.ADVANCED_RESUME_INIT -> {
                    log.info("고급 이력서 init 요청 성공 및 SSE 송신 시작");
                    AdvancedResumeInitEvent event = objectMapper.readValue(
                        json, AdvancedResumeInitEvent.class
                    );
                    sseService.pushAdvancedResumeInit(event.memberId(), event.result());
                }

                case Channel.ADVANCED_RESUME_UPDATE -> {
                    log.info("고급 이력서 Update 요청 성공 및 SSE 송신 시작");
                    AdvancedResumeUpdateEvent event = objectMapper.readValue(
                        json, AdvancedResumeUpdateEvent.class
                    );
                    sseService.pushAdvancedResumeUpdate(event.memberId(), event.result());
                }

                case Channel.PROBLEM_FEEDBACK -> {
                    log.info("면접문제 피드백 요청 성공 및 SSE 송신 시작");
                    FeedbackEvent event = objectMapper.readValue(
                        json, FeedbackEvent.class
                    );
                    sseService.pushProblemFeedback(event.memberId(), event.result());
                }

                case Channel.AI_ERROR_CHANNEL -> {
                    log.info("비동기 처리중 예외 발생");
                    AiErrorEvent event = objectMapper.readValue(json, AiErrorEvent.class);

                    sseService.pushError(
                        event.memberId(),
                        event.eventName(),
                        event.message()
                    );
                }

                default -> log.warn("[RedisSubscriber] 알 수 없는 채널 수신: {}", channel);
            }

        } catch (Exception e) {
            log.error("[RedisSubscriber] 채널 {} 메시지 처리 실패", channel, e);
        }
    }
}
