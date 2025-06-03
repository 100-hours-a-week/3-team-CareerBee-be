package org.choon.careerbee.domain.member.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

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
            req.certificationCount(),
            req.projectCount(),
            req.majorType(),
            req.companyName(),
            req.workPeriod(),
            req.position(),
            req.additionalExperiences()
        );
    }
}