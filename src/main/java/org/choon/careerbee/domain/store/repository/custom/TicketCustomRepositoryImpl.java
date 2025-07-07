package org.choon.careerbee.domain.store.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TicketCustomRepositoryImpl implements TicketCustomRepository {

    private final JPAQueryFactory queryFactory;
}
