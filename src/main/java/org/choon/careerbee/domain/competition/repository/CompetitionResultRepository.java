package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.repository.custom.result.CompetitionResultCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionResultRepository extends
    JpaRepository<CompetitionResult, Long>, CompetitionResultCustomRepository {

    boolean existsByMemberIdAndCompetitionId(Long memberId, Long competitionId);
}
