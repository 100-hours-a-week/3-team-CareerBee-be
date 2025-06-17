package org.choon.careerbee.domain.notification.schedule;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseRunner {

    private final SseService sseService;

    @Scheduled(fixedRate = 3000)
    public void sendPing() {
        sseService.sendPingToAll();
    }
}
