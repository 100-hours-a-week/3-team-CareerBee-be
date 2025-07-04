package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.repository.custom.summary.CompetitionSummaryCustomRepository;
import org.choon.careerbee.domain.competition.repository.jdbc.CompetitionSummaryJdbcRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionSummaryRepository extends
    JpaRepository<CompetitionSummary, Long>,
    CompetitionSummaryCustomRepository,
    CompetitionSummaryJdbcRepository
{

}
