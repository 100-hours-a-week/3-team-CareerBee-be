package org.choon.careerbee.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        log.info("sse 연결 요청 : {}", principalDetails.getId());
        return sseService.connect(principalDetails.getId());
    }
}
