package org.choon.careerbee.domain.competition.repository.custom.competition;

import static org.choon.careerbee.domain.competition.domain.problem.QCompetitionProblem.competitionProblem;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.dto.internal.ProblemAnswerInfo;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CompetitionProblemCustomRepositoryImpl implements CompetitionProblemCustomRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<ProblemAnswerInfo> getProblemAnswerInfoByCompetitionId(Long competitionId) {
        return queryFactory.select(
                Projections.constructor(
                    ProblemAnswerInfo.class,
                    competitionProblem.id,
                    competitionProblem.answer,
                    competitionProblem.solution
                ))
            .from(competitionProblem)
            .where(competitionProblem.competition.id.eq(competitionId))
            .fetch();
    }
}
