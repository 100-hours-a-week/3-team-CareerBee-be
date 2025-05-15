package org.choon.careerbee.domain.member.repository.custom;

import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public MyInfoResp fetchMyInfoByMemberId(Long memberId) {
        return queryFactory
            .select(Projections.constructor(
                MyInfoResp.class,
                member.nickname
            ))
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
    }
}
