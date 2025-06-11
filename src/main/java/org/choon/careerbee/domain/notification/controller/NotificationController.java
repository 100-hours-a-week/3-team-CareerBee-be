package org.choon.careerbee.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.notification.service.NotificationCommandService;
import org.choon.careerbee.domain.notification.service.NotificationQueryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members/notifications")
@RestController
public class NotificationController {

    private final NotificationCommandService commandService;
    private final NotificationQueryService queryService;

    
}
