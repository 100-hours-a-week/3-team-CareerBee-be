package org.choon.careerbee.domain.company.repository.wish.custom;

import static org.choon.careerbee.domain.company.entity.wish.QWishCompany.wishCompany;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class WishCompanyCustomRepositoryImpl implements WishCompanyCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public WishCompanyIdResp fetchWishCompanyIdsByMember(Member member) {
        List<Long> companyIds = queryFactory
            .select(wishCompany.company.id)
            .from(wishCompany)
            .where(wishCompany.member.id.eq(member.getId()))
            .fetch();

        return new WishCompanyIdResp(companyIds);
    }
}
