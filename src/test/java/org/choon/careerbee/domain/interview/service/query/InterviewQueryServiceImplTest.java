package org.choon.careerbee.domain.interview.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewQueryServiceImplTest {

    @Mock
    private InterviewProblemRepository problemRepository;

    @Mock
    private SolvedInterviewProblemRepository solvedProblemRepository;

    @InjectMocks
    private InterviewQueryServiceImpl interviewQueryService;

    @Test
    @DisplayName("면접 문제를 타입별로 조회 - 정상 응답 반환")
    void fetchInterviewProblem_shouldReturnInterviewProblemResp() {
        // given
        List<InterviewProblemInfo> mockProblems = List.of(
            new InterviewProblemInfo(ProblemType.BACKEND, "백엔드 문제입니다."),
            new InterviewProblemInfo(ProblemType.FRONTEND, "프론트엔드 문제입니다."),
            new InterviewProblemInfo(ProblemType.DEVOPS, "데브옵스 문제입니다."),
            new InterviewProblemInfo(ProblemType.AI, "AI 문제입니다.")
        );

        when(problemRepository.fetchFirstInterviewProblemsByType())
            .thenReturn(mockProblems);

        // when
        InterviewProblemResp result = interviewQueryService.fetchInterviewProblem();

        // then
        verify(problemRepository, times(1)).fetchFirstInterviewProblemsByType();
        assertThat(result.interviewProblems()).hasSize(4);

        assertThat(result.interviewProblems()).anySatisfy(problem -> {
            assertThat(problem.type()).isEqualTo(ProblemType.BACKEND);
            assertThat(problem.question()).isEqualTo("백엔드 문제입니다.");
        });
        assertThat(result.interviewProblems()).anySatisfy(problem -> {
            assertThat(problem.type()).isEqualTo(ProblemType.FRONTEND);
            assertThat(problem.question()).isEqualTo("프론트엔드 문제입니다.");
        });
    }
}
