package org.choon.careerbee.domain.interview.repository;

import java.util.Optional;
import org.choon.careerbee.domain.interview.domain.SolvedInterviewProblem;
import org.choon.careerbee.domain.interview.repository.custom.SolvedInterviewProblemCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedInterviewProblemRepository extends
    JpaRepository<SolvedInterviewProblem, Long>, SolvedInterviewProblemCustomRepository {

    boolean existsByMemberIdAndInterviewProblemId(Long memberId, Long interviewProblemId);

    Optional<SolvedInterviewProblem> findByMemberIdAndInterviewProblemId(Long memberId,
        Long InterviewProblemId);
}
