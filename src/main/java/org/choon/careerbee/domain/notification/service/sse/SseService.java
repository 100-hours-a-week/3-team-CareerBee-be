package org.choon.careerbee.domain.notification.service.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter connect(Long memberId);

    void sendTo(Long memberId);

    void sendAll();

    void sendPingToAll();
}
