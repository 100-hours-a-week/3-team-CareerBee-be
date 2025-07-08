package org.choon.careerbee.domain.interview.repository;

import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.repository.custom.SolvedInterviewProblemCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedInterviewProblemRepository extends
    JpaRepository<SolvedInterviewProblem, Long>, SolvedInterviewProblemCustomRepository {

}
