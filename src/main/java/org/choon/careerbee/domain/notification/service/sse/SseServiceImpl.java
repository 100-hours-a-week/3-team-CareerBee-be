package org.choon.careerbee.domain.notification.service.sse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
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
    public void sendTo(Long memberId, Object data) {
        log.info("알림 전송 시도. id : {}", memberId);
        SseEmitter emitter = emitters.get(memberId);

        if (emitter == null) {
            log.warn("[SSE No Connection] memberId={}", memberId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                .name(NOTIFICATION)
                .data(data));
            log.info("[SSE Success] memberId={}", memberId);
        } catch (IOException ex) {
            log.warn("[SSE Broken] memberId={} -> remove emitter", memberId);
            emitter.completeWithError(ex);
            emitters.remove(memberId);
        }
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
                log.info("[SSE Ping Success] {}에게 전송 성공", memberId);
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("[SSE Fail] {}에게 Ping 전송 실패, emitter 제거", memberId);
            }
        });
    }


}
