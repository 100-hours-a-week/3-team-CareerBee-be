package org.choon.careerbee.domain.competition.repository.custom;

import static org.choon.careerbee.domain.competition.domain.problem.QCompetitionProblem.competitionProblem;
import static org.choon.careerbee.domain.competition.domain.problem.QProblemChoice.problemChoice;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp.ProblemChoiceInfo;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp.ProblemInfo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompetitionCustomRepositoryImpl implements CompetitionCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CompetitionProblemResp fetchCompetitionProblemsByCompetitionId(Long competitionId) {
        // 1. 문제 + 보기 선택지 JOIN
        List<Tuple> results = queryFactory
            .select(competitionProblem, problemChoice)
            .from(competitionProblem)
            .leftJoin(problemChoice).on(problemChoice.competitionProblem.eq(competitionProblem))
            .where(competitionProblem.competition.id.eq(competitionId))
            .orderBy(competitionProblem.id.asc(), problemChoice.choiceOrder.asc())
            .fetch();

        // 2. 선택지 그룹핑
        Map<Long, List<ProblemChoiceInfo>> choiceMap = results.stream()
            .filter(tuple -> tuple.get(problemChoice) != null)
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(competitionProblem).getId(),
                Collectors.mapping(
                    tuple -> new ProblemChoiceInfo(
                        tuple.get(problemChoice).getChoiceOrder(),
                        tuple.get(problemChoice).getContent()
                    ),
                    Collectors.toList()
                )
            ));

        // 3. 문제 정보 구성
        List<CompetitionProblemResp.ProblemInfo> problemInfos = results.stream()
            .map(tuple -> tuple.get(competitionProblem))
            .distinct()
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int[] index = {1};
                    return list.stream()
                        .map(problem -> new ProblemInfo(
                            index[0]++, // 순서대로 number 부여
                            problem.getTitle(),
                            problem.getDescription(),
                            problem.getSolution(),
                            problem.getAnswer().intValue(),
                            choiceMap.getOrDefault(problem.getId(), List.of())
                        ))
                        .collect(Collectors.toList());
                }
            ));

        return new CompetitionProblemResp(problemInfos);
    }
}
