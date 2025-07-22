package org.choon.careerbee.domain.interview.service.query;

import static org.choon.careerbee.util.redis.RedisUtil.getOrDefaultBoolean;
import static org.choon.careerbee.util.redis.RedisUtil.getOrInitCount;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.CheckProblemSolveResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemDetailResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.dto.response.MemberInterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.MemberSolveAvailability;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.util.date.TimeUtil;
import org.choon.careerbee.util.redis.RedisKeyFactory;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InterviewQueryServiceImpl implements InterviewQueryService {

    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;
    private final RedissonClient redissonClient;

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

    @Override
    public SaveInterviewProblemResp fetchSaveInterviewProblem(
        Long accessMemberId, Long cursor, int size
    ) {
        return solvedProblemRepository.fetchSaveProblemIdsByMemberId(accessMemberId, cursor, size);
    }

    @Override
    public InterviewProblemDetailResp fetchMemberInterviewProblemByType(
        ProblemType problemType, Long accessMemberId
    ) {
        long ttl = TimeUtil.getSecondsUntilMidnight();

        String canSolveKey = RedisKeyFactory.canSolveKey(accessMemberId, problemType);
        String freeCountKey = RedisKeyFactory.freeCountKey(accessMemberId, problemType);
        String payCountKey = RedisKeyFactory.payCountKey(accessMemberId, problemType);

        // canSolve: null이면 true로 초기화
        Boolean canSolve = getOrDefaultBoolean(redissonClient, canSolveKey, true, ttl);

        // count: null이면 0으로 초기화
        Integer freeCount = getOrInitCount(redissonClient, freeCountKey, ttl);
        Integer payCount = getOrInitCount(redissonClient, payCountKey, ttl);

        // 문제 조회
        MemberInterviewProblemResp problemResp = canSolve
            ? solvedProblemRepository.fetchNextProblemByTypeAndMemberId(problemType, accessMemberId)
            : solvedProblemRepository.fetchSolveProblemInfoByTypeAndMemberId(problemType,
                accessMemberId);

        // 풀이 가능 여부 결정
        boolean canSolveFree = (freeCount == 0);
        boolean canSolvePay = (freeCount == 1 && payCount < 2);

        return new InterviewProblemDetailResp(
            problemResp,
            new MemberSolveAvailability(canSolveFree, canSolvePay)
        );
    }
}
