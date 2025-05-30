package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.CompetitionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionParticipantRepository extends
    JpaRepository<CompetitionParticipant, Long> {

    boolean existsByMemberIdAndCompetitionId(Long memberId, Long competitionId);

}
