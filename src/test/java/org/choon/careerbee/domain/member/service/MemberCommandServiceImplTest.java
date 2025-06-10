package org.choon.careerbee.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;
import org.choon.careerbee.domain.member.progress.ResumeProgressPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    ResumeProgressPolicy progressPolicy;

    @InjectMocks
    private MemberCommandServiceImpl memberCommandService;

    @Test
    @DisplayName("이력 정보 업데이트 → 필드·progress 반영 확인")
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
        when(progressPolicy.calculate(mockMember)).thenReturn(320);

        // when
        memberCommandService.updateResumeInfo(req, accessMemberId);

        // then
        assertAll(
            () -> assertThat(mockMember.getPreferredJob()).isEqualTo(PreferredJob.BACKEND),
            () -> assertThat(mockMember.getPsTier()).isEqualTo("GL3"),
            () -> assertThat(mockMember.getProjectCount()).isEqualTo(2),
            () -> assertThat(mockMember.getProgress()).isEqualTo(320)    // 계산 결과
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
        order.verify(mockMember).recalcProgress(progressPolicy);
        order.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("내 정보 수정 - 정상 수행 시 Member의 updateProfileInfo 호출")
    void updateProfileInfo_shouldCallMemberUpdate() {
        // given
        Long accessMemberId = 1L;

        UpdateProfileInfoReq req = new UpdateProfileInfoReq(
            "https://example.com/profile.png",
            "new_email@test.com",
            "새닉네임"
        );

        Member mockMember = mock(Member.class);
        when(memberQueryService.findById(accessMemberId)).thenReturn(mockMember);

        // when
        memberCommandService.updateProfileInfo(req, accessMemberId);

        // then
        verify(memberQueryService, times(1)).checkEmailExist(req.newEmail());
        verify(memberQueryService, times(1)).findById(accessMemberId);
        verify(mockMember, times(1)).updateProfileInfo(
            argThat(command ->
                command.email().equals(req.newEmail()) &&
                    command.nickname().equals(req.newNickname()) &&
                    command.profileImgUrl().equals(req.newProfileUrl())
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
