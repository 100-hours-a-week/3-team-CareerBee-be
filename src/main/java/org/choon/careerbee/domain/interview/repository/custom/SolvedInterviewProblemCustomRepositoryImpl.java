package org.choon.careerbee.domain.interview.repository.custom;

import static org.choon.careerbee.domain.interview.domain.QInterviewProblem.interviewProblem;
import static org.choon.careerbee.domain.interview.domain.QSolvedInterviewProblem.solvedInterviewProblem;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.interview.dto.response.ProblemInfo;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp.SaveProblemInfo;
import org.choon.careerbee.domain.interview.dto.response.SolveInfo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SolvedInterviewProblemCustomRepositoryImpl implements
    SolvedInterviewProblemCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public SaveInterviewProblemResp fetchSaveProblemIdsByMemberId(
        Long memberId, Long cursor, int size
    ) {
        List<SaveProblemInfo> solvedProblemInfos = queryFactory
            .select(
                Projections.constructor(
                    SaveProblemInfo.class,
                    interviewProblem.id,
                    interviewProblem.type,
                    interviewProblem.question,
                    solvedInterviewProblem.answer,
                    solvedInterviewProblem.feedback
                )
            )
            .from(solvedInterviewProblem)
            .where(
                solvedInterviewProblem.member.id.eq(memberId),
                solvedInterviewProblem.saveStatus.eq(SaveStatus.SAVED),
                cursorCondition(cursor)
            )
            .join(solvedInterviewProblem.interviewProblem, interviewProblem)
            .orderBy(solvedInterviewProblem.id.desc())
            .limit(size + 1)
            .fetch();

        boolean hasNext = solvedProblemInfos.size() > size;
        if (hasNext) {
            solvedProblemInfos.remove(size);
        }

        Long nextCursor = hasNext ? solvedProblemInfos.getLast().id() : null;

        return new SaveInterviewProblemResp(solvedProblemInfos, nextCursor, hasNext);
    }

    @Override
    public SolveInfo fetchSolveProblemInfoByTypeAndMemberId(
        ProblemType problemType, Long memberId
    ) {
        return queryFactory
            .select(
                Projections.constructor(
                    SolveInfo.class,
                    interviewProblem.id,
                    interviewProblem.question,
                    solvedInterviewProblem.answer,
                    solvedInterviewProblem.feedback,
                    solvedInterviewProblem.saveStatus
                ))
            .from(solvedInterviewProblem)
            .where(
                solvedInterviewProblem.member.id.eq(memberId),
                solvedInterviewProblem.interviewProblem.type.eq(problemType)
            )
            .join(solvedInterviewProblem.interviewProblem, interviewProblem)
            .orderBy(solvedInterviewProblem.createdAt.desc())
            .limit(1)
            .fetchOne();
    }

    @Override
    public ProblemInfo fetchNextProblemByTypeAndMemberId(
        ProblemType problemType, Long memberId
    ) {
        Long lastSolvedProblemId = queryFactory
            .select(interviewProblem.id)
            .from(solvedInterviewProblem)
            .join(solvedInterviewProblem.interviewProblem, interviewProblem)
            .where(
                solvedInterviewProblem.member.id.eq(memberId),
                interviewProblem.type.eq(problemType)
            )
            .orderBy(solvedInterviewProblem.createdAt.desc())
            .limit(1)
            .fetchOne();

        if (lastSolvedProblemId != null) {
            return queryFactory
                .select(Projections.constructor(
                    ProblemInfo.class,
                    interviewProblem.id,
                    interviewProblem.question
                ))
                .from(interviewProblem)
                .where(
                    interviewProblem.type.eq(problemType),
                    interviewProblem.id.gt(lastSolvedProblemId)
                )
                .orderBy(interviewProblem.id.asc())
                .limit(1)
                .fetchOne();
        } else {
            // 한 번도 푼 적이 없다면 가장 첫 문제를 리턴
            return queryFactory
                .select(Projections.constructor(
                    ProblemInfo.class,
                    interviewProblem.id,
                    interviewProblem.question
                ))
                .from(interviewProblem)
                .where(interviewProblem.type.eq(problemType))
                .orderBy(interviewProblem.id.asc())
                .limit(1)
                .fetchOne();
        }
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null
            ? interviewProblem.id.lt(cursor)
            : null;
    }
}
