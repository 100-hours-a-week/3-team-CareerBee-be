package org.choon.careerbee.domain.member.service;

import static org.mockito.ArgumentMatchers.argThat;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {

    @Mock
    private MemberQueryService memberQueryService;

    @InjectMocks
    private MemberCommandServiceImpl memberCommandService;

    @Test
    @DisplayName("이력 정보 업데이트 - 정상 수행 시 Member의 updateResumeInfo 호출")
    void updateResumeInfo_shouldCallMemberUpdate() {
        // given
        Long accessMemberId = 1L;

        UpdateResumeReq req = new UpdateResumeReq(
            "BR1",
            2,
            3,
            MajorType.MAJOR,
            "스타트업",
            12,
            "백엔드 개발자",
            "추가 경험 내용"
        );

        Member mockMember = mock(Member.class);
        when(memberQueryService.findById(accessMemberId)).thenReturn(mockMember);

        // when
        memberCommandService.updateResumeInfo(req, accessMemberId);

        // then
        verify(mockMember, times(1)).updateResumeInfo(
            req.psTier(),
            req.certificationCount(),
            req.projectCount(),
            req.majorType(),
            req.companyName(),
            req.workPeriod(),
            req.position(),
            req.additionalExperiences()
        );
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
