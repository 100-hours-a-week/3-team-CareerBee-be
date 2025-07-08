package org.choon.careerbee.domain.interview.service.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InterviewQueryServiceImpl implements InterviewQueryService {

    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;

    @Override
    public InterviewProblemResp fetchInterviewProblem() {
        List<InterviewProblemInfo> problemInfos = problemRepository.fetchFirstInterviewProblemsByType();
        return new InterviewProblemResp(problemInfos);
    }

    @Override
    public CheckProblemSolveResp checkInterviewProblemSolved(
        Long problemId, Long accessMemberId
    ) {
        return new CheckProblemSolveResp(
            solvedProblemRepository.existsByMemberIdAndInterviewProblemId(accessMemberId, problemId)
        );
    }

    @Override
    public InterviewProblem findById(Long problemId) {
        return problemRepository.findById(problemId)
            .orElseThrow(
                () -> new CustomException(CustomResponseStatus.INTERVIEW_PROBLEM_NOT_EXIST));
    }

    @Override
    public SolvedInterviewProblem findSolvedProblemById(Long problemId, Long memberId) {
        return solvedProblemRepository.findByMemberIdAndInterviewProblemId(memberId, problemId)
            .orElseThrow(
                () -> new CustomException(CustomResponseStatus.SOLVED_INTERVIEW_PROBLEM_NOT_EXIST));
    }
}
