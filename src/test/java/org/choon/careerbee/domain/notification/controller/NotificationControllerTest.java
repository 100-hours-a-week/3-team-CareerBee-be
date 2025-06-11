package org.choon.careerbee.domain.notification.controller;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.NotificationFixture.createNotification;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
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
    private EntityManager em;

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
        notifications.forEach(em::persist);
        em.flush();
        em.clear();

        // when & then
        mockMvc.perform(get("/api/v1/members/notifications")
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.httpStatusCode")
                .value(CustomResponseStatus.SUCCESS.getHttpStatusCode()))
            .andExpect(jsonPath("$.message")
                .value("알림 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.notifications.length()").value(5)) // 기본 size = 5
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

        em.persist(n1);
        em.persist(n2);
        em.persist(n3);
        em.persist(n4);
        em.persist(n5);
        em.persist(n6);
        em.flush();

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
}
