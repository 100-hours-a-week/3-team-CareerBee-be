package org.choon.careerbee.domain.interview.service.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewCommandServiceImplTest {

    @Mock
    private InterviewQueryService queryService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private InterviewProblemRepository problemRepository;

    @Mock
    private SolvedInterviewProblemRepository solvedProblemRepository;

    @InjectMocks
    private InterviewCommandServiceImpl commandService;

    @Test
    @DisplayName("면접 문제 저장 - 이미 푼 문제인 경우 save() 호출")
    void saveInterviewProblem_shouldCallSaveOnSolvedProblem() {
        // given
        Long memberId = 1L;
        Long problemId = 100L;

        SolvedInterviewProblem mockSolvedProblem = mock(SolvedInterviewProblem.class);
        when(queryService.findSolvedProblemById(problemId, memberId))
            .thenReturn(mockSolvedProblem);

        // when
        commandService.saveInterviewProblem(problemId, memberId);

        // then
        verify(queryService, times(1)).findSolvedProblemById(problemId, memberId);
        verify(mockSolvedProblem, times(1)).save();
    }
}
