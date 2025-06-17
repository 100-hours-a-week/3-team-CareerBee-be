package org.choon.careerbee.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NotificationCommandServiceImplTest {

    @Mock
    private NotificationQueryService queryService;

    @InjectMocks
    private NotificationCommandServiceImpl commandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markAsRead_success() {
        // given
        Long memberId = 1L;
        List<Long> ids = Arrays.asList(101L, 102L);
        ReadNotificationReq request = new ReadNotificationReq(ids);

        Notification noti1 = mock(Notification.class);
        Notification noti2 = mock(Notification.class);
        List<Notification> fetched = Arrays.asList(noti1, noti2);

        when(queryService.fetchNotificationInIds(ids, memberId)).thenReturn(fetched);

        // when
        commandService.markAsRead(memberId, request);

        // then
        verify(noti1).markAsRead();
        verify(noti2).markAsRead();
    }

    @Test
    @DisplayName("알림 읽음 처리 - 일부 알림이 없거나 권한 없음 → 예외 발생")
    void markAsRead_invalidAccess() {
        // given
        Long memberId = 1L;
        List<Long> ids = Arrays.asList(101L, 102L, 103L);
        ReadNotificationReq request = new ReadNotificationReq(ids);

        Notification noti1 = mock(Notification.class);
        Notification noti2 = mock(Notification.class);
        List<Notification> fetched = Arrays.asList(noti1, noti2);

        when(queryService.fetchNotificationInIds(ids, memberId)).thenReturn(fetched);

        // when & then
        assertThatThrownBy(() -> commandService.markAsRead(memberId, request))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.NOTIFICATION_UPDATE_INVALID.getMessage());
    }
}
