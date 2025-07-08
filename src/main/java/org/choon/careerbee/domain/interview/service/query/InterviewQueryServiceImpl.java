package org.choon.careerbee.domain.interview.service.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQueryServiceImpl implements InterviewQueryService {

    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;

    @Override
    public InterviewProblemResp fetchInterviewProblem() {
        List<InterviewProblemInfo> problemInfos = problemRepository.fetchFirstInterviewProblemsByType();
        return new InterviewProblemResp(problemInfos);
    }
}
