package org.choon.careerbee.domain.interview.service.command;

import static org.choon.careerbee.util.redis.RedisKeyFactory.canSolveKey;
import static org.choon.careerbee.util.redis.RedisKeyFactory.freeCountKey;
import static org.choon.careerbee.util.redis.RedisKeyFactory.payCountKey;
import static org.choon.careerbee.util.redis.RedisUtil.getOrDefaultBoolean;
import static org.choon.careerbee.util.redis.RedisUtil.getOrInitCount;
import static org.choon.careerbee.util.redis.RedisUtil.setBoolean;
import static org.choon.careerbee.util.redis.RedisUtil.setCount;

import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.common.pubsub.RedisPublisher;
import org.choon.careerbee.common.pubsub.dto.FeedbackEvent;
import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.request.AiFeedbackReq;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackRespFromAi;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.service.sse.SseService;
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
    private final InterviewProblemRepository problemRepository;
    private final SolvedInterviewProblemRepository solvedProblemRepository;
    private final AiApiClient aiApiClient;
    private final RedissonClient redissonClient;
    private final Clock clock;
    private final SseService sseService;
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
    public AiFeedbackRespFromAi submitAnswer(SubmitAnswerReq req, Long accessMemberId) {
        Member member = memberQueryService.findById(accessMemberId);
        InterviewProblem problem = interviewQueryService.findById(req.problemId());
        member.plusPoint(SOLVE_POINT);

        if (interviewQueryService.checkInterviewProblemSolved(req.problemId(), accessMemberId)
            .isSolved()
        ) {
            throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PROBLEM);
        }

        long ttl = TimeUtil.getSecondsUntilMidnight();

        String canSolveKey = canSolveKey(accessMemberId, req.type());
        String freeCountKey = freeCountKey(accessMemberId, req.type());
        String payCountKey = payCountKey(accessMemberId, req.type());

        if (req.isFreeProblem()) {
            // 무료 문제 제출
            Integer count = getOrInitCount(redissonClient, freeCountKey, ttl);
            if (count == 1) {
                throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_FREE_PROBLEM);
            }
            setCount(redissonClient, freeCountKey, 1, ttl);
        } else {
            // 유료 문제 제출
            int count = getOrInitCount(redissonClient, payCountKey, ttl);
            if (count >= 2) {
                throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
            }
            setCount(redissonClient, payCountKey, count + 1, ttl);
        }

        setBoolean(redissonClient, canSolveKey, false, ttl);

        AiFeedbackRespFromAi feedbackResp = aiApiClient.requestFeedback(
            AiFeedbackReq.of(accessMemberId, req.question(), req.answer())
        );

        solvedProblemRepository.save(
            SolvedInterviewProblem.of(
                member, problem, req.answer(),
                feedbackResp.feedback(), SaveStatus.UNSAVED
            )
        );

        return feedbackResp;
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

        CompletableFuture<AiFeedbackResp> future = new CompletableFuture<>();

        // 중복 풀이 방지
        if (interviewQueryService.checkInterviewProblemSolved(req.problemId(), accessMemberId)
            .isSolved()
        ) {
            future.completeExceptionally(
                new CustomException(CustomResponseStatus.ALREADY_SOLVED_PROBLEM));
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
                    throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_FREE_PROBLEM);
                }
                setCount(redissonClient, freeCountKey, 1, ttl);
            } else {
                int count = getOrInitCount(redissonClient, payCountKey, ttl);
                if (count >= 2) {
                    throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
                }
                setCount(redissonClient, payCountKey, count + 1, ttl);
            }

            setBoolean(redissonClient, canSolveKey, false, ttl);
        } catch (Exception ex) {
            future.completeExceptionally(ex);
            return;
        }

        log.info("피드백 요청하는 로직 시작");
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
                future.completeExceptionally(ex);
                log.error("비동기 피드백 생성 실패", ex);
                return null;
            });
    }

}
