package org.choon.careerbee.domain.member.repository.custom;

import static org.choon.careerbee.domain.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
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
                member.imgUrl, // Todo : badge 엔티티 추가 후 해당 뱃지 이미지로 변경
                member.imgUrl, // Todo : 프로필 프레임 추가 후 변경
                Expressions.constant(false), // Todo : 알림 기능 생성시 변경
                member.points
            ))
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();
    }

    @Override
    public Optional<String> getNicknameByMemberId(Long memberId) {
        return Optional.ofNullable(
            queryFactory
                .select(member.nickname)
                .from(member)
                .where(member.id.eq(memberId))
                .fetchOne()
        );
    }

    @Override
    public List<Long> findAllMemberIds() {
        return queryFactory
            .select(member.id)
            .from(member)
            .fetch();
    }
}
