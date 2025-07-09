package org.choon.careerbee.domain.interview.repository.custom;

import static org.choon.careerbee.domain.interview.domain.QInterviewProblem.interviewProblem;
import static org.choon.careerbee.domain.interview.domain.QSolvedInterviewProblem.solvedInterviewProblem;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp;
import org.choon.careerbee.domain.interview.dto.response.SaveInterviewProblemResp.SaveProblemInfo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null
            ? solvedInterviewProblem.id.lt(cursor)
            : null;
    }
}
