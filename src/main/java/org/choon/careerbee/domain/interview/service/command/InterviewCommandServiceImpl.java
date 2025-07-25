package org.choon.careerbee.domain.interview.service.command;

import static org.choon.careerbee.util.redis.RedisKeyFactory.canSolveKey;
import static org.choon.careerbee.util.redis.RedisKeyFactory.freeCountKey;
import static org.choon.careerbee.util.redis.RedisKeyFactory.payCountKey;
import static org.choon.careerbee.util.redis.RedisUtil.getOrDefaultBoolean;
import static org.choon.careerbee.util.redis.RedisUtil.getOrInitCount;
import static org.choon.careerbee.util.redis.RedisUtil.setBoolean;
import static org.choon.careerbee.util.redis.RedisUtil.setCount;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.common.pubsub.RedisPublisher;
import org.choon.careerbee.common.pubsub.dto.AiErrorEvent;
import org.choon.careerbee.common.pubsub.dto.FeedbackEvent;
import org.choon.careerbee.common.pubsub.enums.EventName;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.request.AiFeedbackReq;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.util.date.TimeUtil;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewCommandServiceImpl implements InterviewCommandService {

    private static final Integer SOLVE_POINT = 1;
    private static final Integer NEXT_PROBLEM_POINT = 1;

    private final InterviewQueryService queryService;
    private final MemberQueryService memberQueryService;
    private final InterviewQueryService interviewQueryService;
    private final SolvedInterviewProblemRepository solvedProblemRepository;
    private final AiApiClient aiApiClient;
    private final RedissonClient redissonClient;
    private final RedisPublisher redisPublisher;

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
    public void requestNextProblem(ProblemType type, Long accessMemberId) {
        Member member = memberQueryService.findById(accessMemberId);
        member.minusPoint(NEXT_PROBLEM_POINT);

        long ttl = TimeUtil.getSecondsUntilMidnight();

        String payCountKey = payCountKey(accessMemberId, type);
        int payCount = getOrInitCount(redissonClient, payCountKey, ttl);

        if (payCount >= 2) {
            throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
        }

        String canSolveKey = canSolveKey(accessMemberId, type);
        boolean canSolve = getOrDefaultBoolean(redissonClient, canSolveKey, true, ttl);

        if (canSolve) {
            throw new CustomException(CustomResponseStatus.ALREADY_HAS_SOLVE_CHANCE);
        }
        setBoolean(redissonClient, canSolveKey, true, ttl);
    }

    @Override
    public void submitAnswerAsync(SubmitAnswerReq req, Long accessMemberId) {
        log.info("피드백 service 로직 실행 시작");
        Member member = memberQueryService.findById(accessMemberId);
        InterviewProblem problem = interviewQueryService.findById(req.problemId());
        member.plusPoint(SOLVE_POINT);

        CompletableFuture<AiFeedbackResp> future = new CompletableFuture<>();

        // 중복 풀이 방지
        if (interviewQueryService.checkInterviewProblemSolved(req.problemId(), accessMemberId)
            .isSolved()
        ) {
            handleAsyncError(
                future, accessMemberId, EventName.PROBLEM_FEEDBACK,
                new CustomException(CustomResponseStatus.ALREADY_SOLVED_PROBLEM),
                "중복 풀이 체크"
            );
            return;
        }

        long ttl = TimeUtil.getSecondsUntilMidnight();

        String canSolveKey = canSolveKey(accessMemberId, req.type());
        String freeCountKey = freeCountKey(accessMemberId, req.type());
        String payCountKey = payCountKey(accessMemberId, req.type());

        try {
            if (req.isFreeProblem()) {
                Integer count = getOrInitCount(redissonClient, freeCountKey, ttl);
                if (count == 1) {
                    log.error("1. 에러 발생");
                    throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_FREE_PROBLEM);
                }
                setCount(redissonClient, freeCountKey, 1, ttl);
            } else {
                int count = getOrInitCount(redissonClient, payCountKey, ttl);
                if (count >= 2) {
                    log.error("2. 에러 발생");
                    throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
                }
                setCount(redissonClient, payCountKey, count + 1, ttl);
            }

            setBoolean(redissonClient, canSolveKey, false, ttl);
        } catch (Exception ex) {
            handleAsyncError(
                future, accessMemberId, EventName.PROBLEM_FEEDBACK,
                ex,
                "문제 피드백"
            );
            return;
        }

        aiApiClient.requestFeedbackAsync(
                AiFeedbackReq.of(accessMemberId, req.question(), req.answer()))
            .thenAccept(result -> {
                log.info("피드백 생성 요청 성공");

                // DB 저장
                solvedProblemRepository.save(
                    SolvedInterviewProblem.of(
                        member, problem, req.answer(),
                        result.feedback(), SaveStatus.UNSAVED
                    )
                );

                AiFeedbackResp aiFeedbackResp = AiFeedbackResp.of(req.problemId(), result);
                // future 완료
                future.complete(aiFeedbackResp);

                // SSE 전달 위한 Redis Pub/Sub
                redisPublisher.publishInterviewProblemFeedbackEvent(
                    new FeedbackEvent(accessMemberId, aiFeedbackResp)
                );
            })
            .exceptionally(ex -> {
                compensateRedisState(req, accessMemberId);
                handleAsyncError(
                    future, accessMemberId, EventName.PROBLEM_FEEDBACK,
                    ex,
                    "문제 피드백"
                );
                return null;
            });
    }

    private void compensateRedisState(SubmitAnswerReq req, Long memberId) {
        long ttl = TimeUtil.getSecondsUntilMidnight();
        String freeCountKey = freeCountKey(memberId, req.type());
        String payCountKey = payCountKey(memberId, req.type());
        String canSolveKey = canSolveKey(memberId, req.type());

        if (req.isFreeProblem()) {
            setCount(redissonClient, freeCountKey, 0, ttl);
        } else {
            int count = getOrInitCount(redissonClient, payCountKey, ttl);
            setCount(redissonClient, payCountKey, Math.max(0, count - 1), ttl);
        }

        setBoolean(redissonClient, canSolveKey, true, ttl);
    }

    private <T> void handleAsyncError(
        CompletableFuture<T> future,
        Long memberId,
        EventName eventName,
        Throwable ex,
        String stepDescription
    ) {
        log.warn("[{}] 비동기 처리 중 에러 발생: {}", stepDescription, ex.getMessage(), ex);
        future.completeExceptionally(ex);
        redisPublisher.publishAiErrorEvent(
            AiErrorEvent.of(memberId, eventName, ex.getMessage())
        );
    }

}
