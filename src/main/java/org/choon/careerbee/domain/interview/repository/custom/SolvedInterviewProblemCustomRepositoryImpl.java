package org.choon.careerbee.domain.interview.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolvedInterviewProblemCustomRepositoryImpl implements
    SolvedInterviewProblemCustomRepository {

    private final JPAQueryFactory queryFactory;
    
}
