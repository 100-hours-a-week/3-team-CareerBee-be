package org.choon.careerbee.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.service.NotificationCommandService;
import org.choon.careerbee.domain.notification.service.NotificationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members/notifications")
@RestController
public class NotificationController {

    private final NotificationCommandService commandService;
    private final NotificationQueryService queryService;

    @GetMapping
    public ResponseEntity<CommonResponse<FetchNotiResp>> fetchMemberNotifications(
        @RequestParam(name = "cursor", required = false) Long cursor,
        @RequestParam(name = "size", defaultValue = "5") int size,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        FetchNotiResp response = queryService.fetchMemberNotifications(
            principalDetails.getId(),
            cursor,
            size
        );

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "알림 조회에 성공하였습니다."
        );
    }
}
