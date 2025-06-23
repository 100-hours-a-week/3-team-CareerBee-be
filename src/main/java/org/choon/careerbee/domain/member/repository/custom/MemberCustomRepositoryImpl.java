package org.choon.careerbee.domain.member.repository.custom;

import static com.querydsl.jpa.JPAExpressions.selectOne;
import static org.choon.careerbee.domain.member.entity.QMember.member;
import static org.choon.careerbee.domain.notification.entity.QNotification.notification;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
                member.nickname,
                member.email,
                member.imgUrl,
                selectOne()
                    .from(notification)
                    .where(
                        notification.member.id.eq(memberId),
                        notification.isRead.isFalse()
                    ).exists(),
                member.points
            ))
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
    }

    @Override
    public String getNicknameByMemberId(Long memberId) {
        return queryFactory
            .select(member.nickname)
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
    }

    @Override
    public List<Long> findAllMemberIds() {
        return queryFactory
            .select(member.id)
            .from(member)
            .fetch();
    }
}
