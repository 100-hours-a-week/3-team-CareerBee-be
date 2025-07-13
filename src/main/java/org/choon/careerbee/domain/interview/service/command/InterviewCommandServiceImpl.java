package org.choon.careerbee.domain.interview.service.command;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.dto.request.AiFeedbackReq;
import org.choon.careerbee.domain.interview.dto.request.SubmitAnswerReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.choon.careerbee.domain.interview.repository.SolvedInterviewProblemRepository;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.service.sse.SseService;
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
        // 1. ai 서버로부터 응답 받기
//        AiFeedbackResp aiFeedbackResp = aiApiClient.requestFeedback(
//            AiFeedbackReq.of(accessMemberId, submitAnswerReq.question(), submitAnswerReq.answer())
//        );

        AiFeedbackResp aiFeedbackResp = aiApiClient.requestFeedback(
            AiFeedbackReq.of(submitAnswerReq.question(), submitAnswerReq.answer())
        );
//        Member member = memberQueryService.findById(accessMemberId);
//        member.plusPoint(SOLVE_POINT);
//
//        long secondsUntilMidnight = Duration.between(LocalTime.now(clock), LocalTime.MAX)
//            .getSeconds();
//        if (submitAnswerReq.isFreeProblem()) {
//            // 2. redis의 freeCount를 1로 세팅
//            RBucket<Integer> freeCountBucket = redissonClient.getBucket(
//                FREE_COUNT_KEY.formatted(accessMemberId));
//            Integer freeCount = freeCountBucket.get();
//
//            if (freeCount != null && freeCount == 1) {
//                throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_FREE_PROBLEM);
//            }
//
//            freeCountBucket.set(1, secondsUntilMidnight, TimeUnit.SECONDS);
//
//            // 3. redis의 canSolve를 false로 세팅
//            redissonClient.getBucket(CAN_SOLVE_KEY)
//                .set(false, secondsUntilMidnight, TimeUnit.SECONDS);
//
//            return aiFeedbackResp;
//        }
//
//        // 2. redis의 payCount를 증가
//        RBucket<Integer> payCountBucket = redissonClient.getBucket(
//            PAY_COUNT_KEY.formatted(accessMemberId));
//        Integer payCount = payCountBucket.get();
//
//        if (payCount >= 2) {
//            throw new CustomException(CustomResponseStatus.ALREADY_SOLVED_PAY_PROBLEM);
//        }
//
//        redissonClient.getBucket(CAN_SOLVE_KEY)
//            .set(false, secondsUntilMidnight, TimeUnit.SECONDS);

        // 4. 답변 생성시 SSE로 프론트로 전달
//        sseService.sendTo(aiFeedbackResp.memberId(), aiFeedbackResp);

        return aiFeedbackResp;
    }
}
