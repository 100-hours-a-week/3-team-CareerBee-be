package org.choon.careerbee.domain.interview.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewCommandServiceImpl implements InterviewCommandService {

    private final InterviewQueryService queryService;
    private final MemberQueryService memberQueryService;
    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;

    @Override
    public void saveInterviewProblem(Long problemIdToSave, Long accessMemberId) {
        SolvedInterviewProblem solvedProblem = queryService.findSolvedProblemById(
            problemIdToSave, accessMemberId
        );

        solvedProblem.save();
    }
}
