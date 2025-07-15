package org.choon.careerbee.domain.interview.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.interview.InterviewProblemFixture.createInterviewProblem;
import static org.choon.careerbee.fixture.interview.SolvedInterviewProblemFixture.createSolvedProblem;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.member.entity.Member;
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

    @Test
    @DisplayName("회원이 해당 면접 문제를 풀었는지 확인 - 문제를 푼 경우 true 반환")
    void checkInterviewProblemSolved_true() {
        // given
        Long memberId = 1L;
        Long problemId = 100L;
        when(solvedProblemRepository.existsByMemberIdAndInterviewProblemId(memberId, problemId))
            .thenReturn(true);

        // when
        CheckProblemSolveResp result =
            interviewQueryService.checkInterviewProblemSolved(problemId, memberId);

        // then
        assertThat(result.isSolved()).isTrue();
        verify(solvedProblemRepository, times(1))
            .existsByMemberIdAndInterviewProblemId(memberId, problemId);
    }

    @Test
    @DisplayName("회원이 해당 면접 문제를 풀었는지 확인 - 풀지 않은 경우 false 반환")
    void checkInterviewProblemSolved_false() {
        // given
        Long memberId = 1L;
        Long problemId = 200L;
        when(solvedProblemRepository.existsByMemberIdAndInterviewProblemId(memberId, problemId))
            .thenReturn(false);

        // when
        CheckProblemSolveResp result =
            interviewQueryService.checkInterviewProblemSolved(problemId, memberId);

        // then
        assertThat(result.isSolved()).isFalse();
        verify(solvedProblemRepository, times(1))
            .existsByMemberIdAndInterviewProblemId(memberId, problemId);
    }

    @Test
    @DisplayName("회원이 푼 면접 문제 조회 - 존재할 경우 반환")
    void findSolvedProblemById_success() {
        // given
        Long memberId = 1L;
        Long problemId = 10L;
        Member mockMember = createMember("testNick", "test@co.kr", 1435L);
        InterviewProblem mockProblem = createInterviewProblem("질문입니다", ProblemType.BACKEND);
        SolvedInterviewProblem mockSolved = createSolvedProblem(
            mockMember, mockProblem, "answer", "feedback", SaveStatus.UNSAVED
        );

        when(solvedProblemRepository.findByMemberIdAndInterviewProblemId(memberId, problemId))
            .thenReturn(Optional.of(mockSolved));

        // when
        SolvedInterviewProblem result =
            interviewQueryService.findSolvedProblemById(problemId, memberId);

        // then
        assertThat(result).isEqualTo(mockSolved);
        verify(solvedProblemRepository, times(1))
            .findByMemberIdAndInterviewProblemId(memberId, problemId);
    }

    @Test
    @DisplayName("회원이 푼 면접 문제 조회 - 없을 경우 예외 발생")
    void findSolvedProblemById_notFound_shouldThrow() {
        // given
        Long memberId = 1L;
        Long problemId = 999L;

        when(solvedProblemRepository.findByMemberIdAndInterviewProblemId(memberId, problemId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interviewQueryService.findSolvedProblemById(problemId, memberId))
            .isInstanceOf(CustomException.class)
            .hasMessage(CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST.getMessage());

        verify(solvedProblemRepository, times(1))
            .findByMemberIdAndInterviewProblemId(memberId, problemId);
    }

    @Test
    @DisplayName("회원이 저장한 면접 문제 목록 조회 - 정상적으로 응답 반환")
    void fetchSaveInterviewProblem_success() {
        // given
        Long memberId = 1L;
        Long cursor = null;
        int size = 10;

        SaveInterviewProblemResp mockResp = new SaveInterviewProblemResp(
            List.of(), null, false
        );

        when(solvedProblemRepository.fetchSaveProblemIdsByMemberId(memberId, cursor, size))
            .thenReturn(mockResp);

        // when
        SaveInterviewProblemResp result =
            interviewQueryService.fetchSaveInterviewProblem(memberId, cursor, size);

        // then
        assertThat(result).isEqualTo(mockResp);
        verify(solvedProblemRepository, times(1))
            .fetchSaveProblemIdsByMemberId(memberId, cursor, size);
    }

}
