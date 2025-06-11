package org.choon.careerbee.domain.notification.controller;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.NotificationFixture.createNotification;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.notification.dto.request.ReadNotificationReq;
import org.choon.careerbee.domain.notification.entity.Notification;
import org.choon.careerbee.domain.notification.entity.enums.NotificationType;
import org.choon.careerbee.domain.notification.repository.NotificationRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private String accessToken;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.saveAndFlush(
            createMember("알림유저", "noti@test.com", 88L));
        accessToken = "Bearer " + jwtUtil.createToken(testMember.getId(), TokenType.ACCESS_TOKEN);
    }

    @Test
    @DisplayName("알림 조회 - 기본 size로 첫 페이지 조회 성공")
    void fetchNotifications_firstPage_success() throws Exception {
        // given
        List<Notification> notifications = List.of(
            createNotification(testMember, "알림1", NotificationType.POINT, true),
            createNotification(testMember, "알림2", NotificationType.POINT, false),
            createNotification(testMember, "알림3", NotificationType.POINT, true),
            createNotification(testMember, "알림4", NotificationType.POINT, false),
            createNotification(testMember, "알림5", NotificationType.POINT, true),
            createNotification(testMember, "알림6", NotificationType.POINT, false)
        );
        notificationRepository.saveAllAndFlush(notifications);

        // when & then
        mockMvc.perform(get("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("알림 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.notifications.length()").value(5))
            .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("알림 조회 - cursor 기반 다음 페이지 조회")
    void fetchNotifications_withCursor_success() throws Exception {
        // given
        Notification n1 = createNotification(testMember, "알림1", NotificationType.POINT, true);
        Notification n2 = createNotification(testMember, "알림2", NotificationType.POINT, false);
        Notification n3 = createNotification(testMember, "알림3", NotificationType.POINT, true);
        Notification n4 = createNotification(testMember, "알림4", NotificationType.POINT, false);
        Notification n5 = createNotification(testMember, "알림5", NotificationType.POINT, true);
        Notification n6 = createNotification(testMember, "알림6", NotificationType.POINT, false);

        notificationRepository.saveAllAndFlush(List.of(n1, n2, n3, n4, n5, n6));

        // when & then
        mockMvc.perform(get("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .param("cursor", String.valueOf(n2.getId()))
                .param("size", "3")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("알림 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.notifications.length()").value(1))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("알림 조회 - 알림이 없는 경우 빈 배열 반환")
    void fetchNotifications_emptyResult() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("알림 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.notifications.length()").value(0))
            .andExpect(jsonPath("$.data.hasNext").value(false))
            .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공적으로 처리되는 경우")
    void markNotificationsAsRead_success() throws Exception {
        // given
        Notification n1 = createNotification(testMember, "알림1", NotificationType.POINT, false);
        Notification n2 = createNotification(testMember, "알림2", NotificationType.POINT, false);
        notificationRepository.saveAllAndFlush(List.of(n1, n2));

        ReadNotificationReq req = new ReadNotificationReq(List.of(n1.getId(), n2.getId()));

        // when & then
        mockMvc.perform(patch("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("알림 읽음 처리에 성공하였습니다."));
    }

    @Test
    @DisplayName("알림 읽음 처리 - 유효하지 않은 알림 ID 포함 시 예외 발생")
    void markNotificationsAsRead_withInvalidId_throwsException() throws Exception {
        // given
        Notification n1 = notificationRepository.saveAndFlush(
            createNotification(testMember, "알림1", NotificationType.POINT, false));

        Long invalidId = 999999L;
        ReadNotificationReq req = new ReadNotificationReq(List.of(n1.getId(), invalidId));

        // when & then
        mockMvc.perform(patch("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.NOTIFICATION_UPDATE_INVALID.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value(CustomResponseStatus.NOTIFICATION_UPDATE_INVALID.getMessage()));
    }
}
