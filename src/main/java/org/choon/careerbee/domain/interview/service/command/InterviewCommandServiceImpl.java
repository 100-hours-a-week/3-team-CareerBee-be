package org.choon.careerbee.domain.interview.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewCommandServiceImpl implements InterviewCommandService {

    private final InterviewQueryService queryService;
    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;

}
