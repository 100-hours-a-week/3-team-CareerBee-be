package org.choon.careerbee.domain.competition.repository;

import org.choon.careerbee.domain.competition.domain.problem.CompetitionProblem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionProblemRepository extends JpaRepository<CompetitionProblem, Long> {

}
