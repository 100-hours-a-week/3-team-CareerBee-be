package org.choon.careerbee.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp.NotificationInfo;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceImplTest {

    @InjectMocks
    private NotificationQueryServiceImpl notificationQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 목록 조회 - 정상 반환")
    void fetchMemberNotifications_success() {
        // given
        Long memberId = 123L;
        Long cursor = 50L;
        int size = 5;

        FetchNotiResp mockResp = new FetchNotiResp(List.of(
            new NotificationInfo(
                1L, NotificationType.COMPETITION, "무무가 1등 했어요",
                LocalDateTime.now(), false
            ),
            new NotificationInfo(
                2L, NotificationType.POINT, "대회 참여 포인트 획득했어요",
                LocalDateTime.now(), false),
            new NotificationInfo(
                3L, NotificationType.RECRUITMENT, "공채가 떴어요",
                LocalDateTime.now(), true)
        ), cursor, true);
        when(notificationRepository.fetchNotificationsByMemberId(memberId, cursor, size))
            .thenReturn(mockResp);

        // when
        FetchNotiResp result = notificationQueryService.fetchMemberNotifications(memberId, cursor,
            size);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockResp);
        verify(notificationRepository, times(1))
            .fetchNotificationsByMemberId(memberId, cursor, size);
    }

    @Test
    @DisplayName("알림 목록 조회 - 알림이 없을 경우 빈배열 반환")
    void fetchMemberNotifications_success_returnEmptyArray() {
        // given
        Long memberId = 123L;
        Long cursor = 50L;
        int size = 5;

        FetchNotiResp mockResp = new FetchNotiResp(List.of(), cursor, true);
        when(notificationRepository.fetchNotificationsByMemberId(memberId, cursor, size))
            .thenReturn(mockResp);

        // when
        FetchNotiResp result = notificationQueryService.fetchMemberNotifications(memberId, cursor,
            size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.notifications().size()).isZero();
        assertThat(result).isEqualTo(mockResp);
        verify(notificationRepository, times(1))
            .fetchNotificationsByMemberId(memberId, cursor, size);
    }
}
