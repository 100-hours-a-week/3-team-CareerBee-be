package org.choon.careerbee.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.image.dto.response.ObjectUrlResp;
import org.choon.careerbee.domain.image.service.ImageService;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private MemberCommandServiceImpl memberCommandService;

    @Test
    @DisplayName("forceJoin - OAuth 정보로 Member 저장 후 반환")
    void forceJoin_success_savesAndReturns() {
        // given
        OAuthInfoResponse oAuth = mock(OAuthInfoResponse.class);
        when(oAuth.getNickname()).thenReturn("kakao-user");
        when(oAuth.getEmail()).thenReturn("kakao@ex.com");
        when(oAuth.getProviderId()).thenReturn(12345L);

        Member persisted = createMember("kakao-user", "kakao@ex.com", 12345L);
        ReflectionTestUtils.setField(persisted, "id", 99L);

        when(memberRepository.save(any(Member.class))).thenReturn(persisted);

        // when
        Member result = memberCommandService.forceJoin(oAuth);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(1)).save(captor.capture());

        Member toSave = captor.getValue();
        assertThat(toSave.getNickname()).isEqualTo("kakao-user");
        assertThat(toSave.getEmail()).isEqualTo("kakao@ex.com");
        assertThat(toSave.getProviderId()).isEqualTo(12345L);
        assertThat(result).isSameAs(persisted);
        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("이력 정보 업데이트 → 필드 반영 확인")
    void updateResumeInfo_shouldCallMemberUpdate() {
        // given
        Long accessMemberId = 1L;

        UpdateResumeReq req = new UpdateResumeReq(
            PreferredJob.BACKEND, "GL3", 4, 2,
            MajorType.MAJOR, "스타트업", 12,
            "백엔드 개발자", "추가 경험"
        );

        Member mockMember = createMember("nickname", "test@test.com", 13L);
        when(memberQueryService.findById(accessMemberId)).thenReturn(mockMember);

        // when
        memberCommandService.updateResumeInfo(req, accessMemberId);

        // then
        assertAll(
            () -> assertThat(mockMember.getPreferredJob()).isEqualTo(PreferredJob.BACKEND),
            () -> assertThat(mockMember.getPsTier()).isEqualTo("GL3"),
            () -> assertThat(mockMember.getProjectCount()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("이력 정보 업데이트 → Member 메서드 호출·순서 확인")
    void updateResumeInfo_callsMemberMethodsInOrder() {
        // given
        Member mockMember = mock(Member.class);
        when(memberQueryService.findById(1L)).thenReturn(mockMember);

        UpdateResumeReq req = new UpdateResumeReq(
            PreferredJob.BACKEND, "GL3", 4, 2,
            MajorType.MAJOR, "스타트업", 12,
            "백엔드 개발자", "추가 경험"
        );

        // when
        memberCommandService.updateResumeInfo(req, 1L);

        // then
        InOrder order = inOrder(mockMember);
        order.verify(mockMember)
            .updateResumeInfo(
                req.preferredJob(), req.psTier(), req.certificationCount(),
                req.projectCount(), req.majorType(), req.companyName(),
                req.workPeriod(), req.position(), req.additionalExperiences()
            );
        order.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("내 정보 수정 - 정상 수행 시 Member의 updateProfileInfo 호출")
    void updateProfileInfo_shouldCallMemberUpdate() {
        // given
        Long accessMemberId = 1L;
        ObjectUrlResp objectUrlResp = new ObjectUrlResp("objectUrl");

        UpdateProfileInfoReq req = new UpdateProfileInfoReq(
            "https://example.com/profile.png",
            "new Nickname"
        );

        Member mockMember = mock(Member.class);
        when(memberQueryService.findById(accessMemberId)).thenReturn(mockMember);
        when(imageService.getObjectUrlByKey(req.newProfileUrl())).thenReturn(objectUrlResp);

        // when
        memberCommandService.updateProfileInfo(req, accessMemberId);

        // then
        verify(memberQueryService, times(1)).findById(accessMemberId);
        verify(mockMember, times(1)).updateProfileInfo(
            argThat(command ->
                command.nickname().equals(req.newNickname()) &&
                    command.profileImgUrl().equals(objectUrlResp.objectUrl())
            ));
    }

    @Test
    @DisplayName("회원 탈퇴 - 정상 수행 시 Member의 withdraw 호출")
    void withdrawal_shouldCallMemberWithdraw() {
        // given
        Long accessMemberId = 1L;
        String reason = "서비스 이용 안함";
        LocalDateTime withdrawAt = LocalDateTime.of(2025, 6, 6, 10, 0);

        WithdrawalReq req = mock(WithdrawalReq.class);
        when(req.withdrawReason()).thenReturn(reason);

        Member mockMember = mock(Member.class);
        when(memberQueryService.findById(accessMemberId)).thenReturn(mockMember);

        // when
        memberCommandService.withdrawal(req, accessMemberId, withdrawAt);

        // then
        verify(mockMember, times(1)).withdraw(
            argThat(command ->
                command.reason().equals(reason) &&
                    command.requestedAt().equals(withdrawAt))
        );
    }
}
