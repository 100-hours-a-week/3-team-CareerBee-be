package org.choon.careerbee.domain.company.repository.wish.custom;

import static org.choon.careerbee.domain.company.entity.QCompany.company;
import static org.choon.careerbee.domain.company.entity.QCompanyKeyword.companyKeyword;
import static org.choon.careerbee.domain.company.entity.wish.QWishCompany.wishCompany;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
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

    @Override
    public WishCompaniesResp fetchWishCompaniesByMemberId(Long memberId, Long cursor, int size) {
        List<CompanySummaryInfo> companySummaryInfos = queryFactory
            .select(
                Projections.constructor(
                    CompanySummaryInfo.class,
                    wishCompany.id,
                    company.name,
                    company.logoUrl,
                    wishCompany.id.count(),
                    Expressions.constant(Collections.emptyList())
                )
            )
            .from(wishCompany)
            .leftJoin(wishCompany)
            .on(wishCompany.company.id.eq(company.id))
            .where(
                wishCompany.member.id.eq(memberId),
                cursorCondition(cursor)
            )
            .groupBy(company.id, company.name, company.logoUrl)
            .orderBy(wishCompany.id.desc())
            .limit(size + 1L)
            .fetch();

        List<CompanySummaryInfo> wishCompanySummaryInfos = new ArrayList<>(
            companySummaryInfos.size());
        for (CompanySummaryInfo companySummaryInfo : companySummaryInfos) {
            Long companyId = companySummaryInfo.id();

            List<CompanySummaryInfo.Keyword> keywords = queryFactory
                .select(Projections.constructor(
                    CompanySummaryInfo.Keyword.class,
                    companyKeyword.content
                ))
                .from(companyKeyword)
                .where(companyKeyword.company.id.eq(companyId))
                .fetch();

            wishCompanySummaryInfos.add(
                new CompanySummaryInfo(
                    companySummaryInfo.id(),
                    companySummaryInfo.name(),
                    companySummaryInfo.logoUrl(),
                    companySummaryInfo.wishCount(),
                    keywords
                )
            );
        }

        boolean hasNext = wishCompanySummaryInfos.size() > size;
        if (hasNext) {
            wishCompanySummaryInfos.remove(size);
        }

        Long nextCursor =
            hasNext ? wishCompanySummaryInfos.getLast().id() : null;

        return new WishCompaniesResp(wishCompanySummaryInfos, nextCursor, hasNext);
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null
            ? wishCompany.id.lt(cursor)
            : null;
    }
}
