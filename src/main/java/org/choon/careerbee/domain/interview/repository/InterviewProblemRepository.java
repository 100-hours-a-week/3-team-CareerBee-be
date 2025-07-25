package org.choon.careerbee.domain.interview.repository;

import org.choon.careerbee.domain.interview.domain.InterviewProblem;
import org.choon.careerbee.domain.interview.repository.custom.InterviewProblemCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewProblemRepository extends
    JpaRepository<InterviewProblem, Long>, InterviewProblemCustomRepository {

}
