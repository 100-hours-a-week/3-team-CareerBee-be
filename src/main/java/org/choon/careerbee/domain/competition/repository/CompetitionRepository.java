package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.repository.custom.CompetitionCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends
    JpaRepository<Competition, Long>, CompetitionCustomRepository {

}
