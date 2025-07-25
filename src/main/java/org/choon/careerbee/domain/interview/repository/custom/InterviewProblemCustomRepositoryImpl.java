package org.choon.careerbee.domain.interview.repository.custom;

import static org.choon.careerbee.domain.interview.domain.QInterviewProblem.interviewProblem;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewProblemCustomRepositoryImpl implements
    InterviewProblemCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InterviewProblemInfo> fetchFirstInterviewProblemsByType() {
        return Arrays.stream(ProblemType.values())
            .map(type -> queryFactory
                .select(Projections.constructor(
                    InterviewProblemInfo.class,
                    interviewProblem.type,
                    interviewProblem.question
                ))
                .from(interviewProblem)
                .where(interviewProblem.type.eq(type))
                .orderBy(interviewProblem.id.asc())
                .limit(1)
                .fetchOne())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
