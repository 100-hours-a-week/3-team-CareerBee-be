package org.choon.careerbee.domain.interview.service.command;

import java.time.Clock;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.request.AiFeedbackReq;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.service.sse.SseService;
import org.choon.careerbee.util.date.TimeUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewCommandServiceImpl implements InterviewCommandService {

    private static final Integer SOLVE_POINT = 1;
    private static final String FREE_COUNT_KEY = "member:%d:ai:freeCount";
    private static final String PAY_COUNT_KEY = "member:%d:ai:payCount";
    private static final String CAN_SOLVE_KEY = "member:%d:ai:canSolve";

    private final InterviewQueryService queryService;
    private final MemberQueryService memberQueryService;
    private final InterviewQueryService interviewQueryService;
    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;
    private final AiApiClient aiApiClient;
    private final RedissonClient redissonClient;
    private final Clock clock;
    private final SseService sseService;

    @Override
    public void saveInterviewProblem(Long problemIdToSave, Long accessMemberId) {
        SolvedInterviewProblem solvedProblem = queryService.findSolvedProblemById(
            problemIdToSave, accessMemberId
        );

        solvedProblem.save();
    }

    @Override
    public void cancelSaveInterviewProblem(Long problemIdToCancelSave, Long accessMemberId) {
        SolvedInterviewProblem solvedProblem = queryService.findSolvedProblemById(
            problemIdToCancelSave, accessMemberId
        );

        solvedProblem.cancelSave();
    }

    @Override
    public AiFeedbackResp submitAnswer(SubmitAnswerReq submitAnswerReq, Long accessMemberId) {
        Member member = memberQueryService.findById(accessMemberId);
        InterviewProblem interviewProblem = interviewQueryService
            .findById(submitAnswerReq.problemId());
        member.plusPoint(SOLVE_POINT);

        if (interviewQueryService.checkInterviewProblemSolved(submitAnswerReq.problemId(),
            accessMemberId).isSolved()) {
            throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PROBLEM);
        }

        String canSolveKey = CAN_SOLVE_KEY.formatted(accessMemberId);
        long secondsUntilMidnight = TimeUtil.getSecondsUntilMidnight();

        if (submitAnswerReq.isFreeProblem()) {
            // 무료 문제를 풀이한 경우
            RBucket<Integer> freeCountBucket = redissonClient.getBucket(
                FREE_COUNT_KEY.formatted(accessMemberId));
            Integer freeCount = freeCountBucket.get();

            if (freeCount != null && freeCount == 1) {
                throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_FREE_PROBLEM);
            }

            freeCountBucket.set(1, secondsUntilMidnight, TimeUnit.SECONDS);
            redissonClient.getBucket(canSolveKey)
                .set(false, secondsUntilMidnight, TimeUnit.SECONDS);
        } else {
            // 유료 문제를 풀이한 경우
            RBucket<Integer> payCountBucket = redissonClient.getBucket(
                PAY_COUNT_KEY.formatted(accessMemberId));
            Integer payCount = payCountBucket.get();
            int count = payCount == null ? 0 : payCount;

            if (count >= 2) {
                throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
            }

            payCountBucket.set(count + 1, secondsUntilMidnight, TimeUnit.SECONDS);
            redissonClient.getBucket(canSolveKey)
                .set(false, secondsUntilMidnight, TimeUnit.SECONDS);
        }

        // ✅ 예외 조건 통과 후에만 AI 요청
        AiFeedbackResp aiFeedbackResp = aiApiClient.requestFeedback(
            AiFeedbackReq.of(accessMemberId, submitAnswerReq.question(), submitAnswerReq.answer())
        );

        solvedProblemRepository.save(
            SolvedInterviewProblem.of(
                member, interviewProblem,
                submitAnswerReq.answer(), aiFeedbackResp.feedback(), SaveStatus.UNSAVED
            )
        );

        return aiFeedbackResp;
    }
}
