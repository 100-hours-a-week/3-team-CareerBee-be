package org.choon.careerbee.domain.notification.service.sse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseServiceImpl implements SseService {

    private static final String NOTIFICATION = "notification";
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));

        log.info("SSE 연결 완료, map 사이즈 : {}", emitters.keySet().size());
        return emitter;
    }

    @Override
    public void sendTo(Long memberId) {
        log.info("알림 전송 시도. id : {}", memberId);
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(NOTIFICATION).data(true));
                log.info("[SSE Success] {}에게 전송 성공", memberId);
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("[SSE Fail] {}에게 전송 실패, emitter 제거", memberId);
            }
        } else {
            log.warn("[SSE No Connection] emitter 객체가 존재하지 않습니다.");
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
}
