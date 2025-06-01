package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionResultRepository extends JpaRepository<CompetitionResult, Long> {

    boolean existsByMemberIdAndCompetitionId(Long memberId, Long competitionId);
}
