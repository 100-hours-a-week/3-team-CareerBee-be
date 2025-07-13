package org.choon.careerbee.domain.notification.service.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService<T> {

    SseEmitter connect(Long memberId);

    void sendTo(Long memberId);

    void sendTo(Long memberId, T data);


    void sendAll();

    void sendPingToAll();
}
