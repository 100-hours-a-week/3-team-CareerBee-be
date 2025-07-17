package org.choon.careerbee.domain.notification.service.sse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeResp;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseServiceImpl implements SseService {

    private static final String NOTIFICATION = "notification";
    private static final String PING = "ping";
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> {
            log.warn("[SSE Time Out] memberId={}", memberId);
            emitter.complete();
            emitters.remove(memberId);
        });
        emitter.onError(e -> {
            log.warn("[SSE Error] memberId={}, msg={}", memberId, e.getMessage());
            emitter.completeWithError(e);
            emitters.remove(memberId);
        });

        emitters.put(memberId, emitter);
        log.info("[SSE Send SUC] SSE 연결 & 알림 전송 성공");
        return emitter;
    }

    @Override
    public void sendTo(Long memberId) {
        log.info("알림 전송 시도. id : {}", memberId);
        SseEmitter emitter = emitters.get(memberId);

        if (emitter == null) {
            log.warn("[SSE No Connection] memberId={}", memberId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                .name(NOTIFICATION)
                .data(true));
            log.info("[SSE Success] memberId={}", memberId);
        } catch (IOException ex) {
            log.warn("[SSE Broken] memberId={} -> remove emitter", memberId);
            emitter.completeWithError(ex);
            emitters.remove(memberId);
        }
    }

    @Override
    public void pushResumeExtracted(Long memberId, ExtractResumeResp resp) {
        sendSseEvent(memberId, "resume-extracted", resp, "이력서 추출");
    }

    @Override
    public void pushAdvancedResumeInit(Long memberId, AdvancedResumeInitResp resp) {
        sendSseEvent(memberId, "advanced-resume-init", resp, "고급 이력서 init");
    }

    @Override
    public void pushAdvancedResumeUpdate(Long memberId, AdvancedResumeResp resp) {
        sendSseEvent(memberId, "advanced-resume-update", resp, "고급 이력서 update");
    }

    @Override
    public void pushProblemFeedback(Long memberId, AiFeedbackResp resp) {
        sendSseEvent(memberId, "problem-feedback", resp, "면접 피드백");
    }

    @Override
    public void sendAll() {
        log.info("[BROADCAST] 전체 알림 전송 시작. 총 {}명", emitters.size());

        emitters.forEach((memberId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(NOTIFICATION).data(true));
                log.info("[SSE Success] {}에게 전송 성공", memberId);
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("[SSE Fail] {}에게 전송 실패, emitter 제거", memberId);
            }
        });

    }

    @Override
    public void sendPingToAll() {
        emitters.forEach((memberId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(PING).data("keep-alive"));
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("[SSE Fail] {}에게 Ping 전송 실패, emitter 제거", memberId);
            }
        });
    }

    private <T> void sendSseEvent(Long memberId, String eventName, T data, String logPrefix) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter == null) {
            log.warn("[SSE] {} - emitter 없음 (memberId={})", logPrefix, memberId);
            return;
        }

        try {
            log.info("[SSE] {} - 전송 시작", logPrefix);
            emitter.send(SseEmitter.event().name(eventName).data(data));
            log.info("[SSE] {} - 전송 완료", logPrefix);
        } catch (IOException e) {
            log.error("[SSE] {} - 전송 실패", logPrefix, e);
            emitter.completeWithError(e);
            emitters.remove(memberId);
        }
    }


}
