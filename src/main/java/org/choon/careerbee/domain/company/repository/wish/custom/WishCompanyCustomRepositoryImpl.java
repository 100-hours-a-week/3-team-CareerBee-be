package org.choon.careerbee.domain.company.repository.wish.custom;

import static org.choon.careerbee.domain.company.entity.QCompany.company;
import static org.choon.careerbee.domain.company.entity.QCompanyKeyword.companyKeyword;
import static org.choon.careerbee.domain.company.entity.wish.QWishCompany.wishCompany;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        // 1. 관심 등록 회사 정보 조회
        List<Tuple> wishCompanyInfos = fetchWishCompanyInfos(memberId, cursor, size);

        // 2. 회사 ID 추출
        List<Long> companyIds = wishCompanyInfos.stream()
            .map(t -> t.get(company.id))
            .toList();

        // 3. 회사별 찜 개수 조회
        Map<Long, Long> wishCountMap = fetchWishCounts(companyIds);

        // 4. 회사 요약 정보 + 키워드 리스트 구성
        List<CompanySummaryInfo> summaryList = wishCompanyInfos.stream()
            .map(tuple -> {
                Long companyId = tuple.get(company.id);
                List<CompanySummaryInfo.Keyword> keywords = fetchCompanyKeywords(companyId);

                return new CompanySummaryInfo(
                    companyId,
                    tuple.get(company.name),
                    tuple.get(company.logoUrl),
                    wishCountMap.getOrDefault(companyId, 0L),
                    keywords
                );
            })
            .collect(Collectors.toList());

        // 5. hasNext & nextCursor 처리
        boolean hasNext = summaryList.size() > size;
        if (hasNext) {
            summaryList.remove(size);
            wishCompanyInfos.remove(size);
        }

        Long nextCursor = hasNext ? wishCompanyInfos.getLast().get(wishCompany.id) : null;

        return new WishCompaniesResp(summaryList, nextCursor, hasNext);
    }

    @Override
    public List<Long> getMemberIdsByCompanyId(Long companyId) {
        return queryFactory
            .select(wishCompany.member.id).distinct()
            .from(wishCompany)
            .where(wishCompany.company.id.eq(companyId))
            .fetch();
    }

    public Map<Long, List<Long>> getWishMemberIdsGroupedByCompanyId(List<Long> companyIds) {
        List<Tuple> result = queryFactory
            .select(wishCompany.company.id, wishCompany.member.id)
            .from(wishCompany)
            .where(wishCompany.company.id.in(companyIds))
            .fetch();

        return result.stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(wishCompany.company.id),
                Collectors.mapping(tuple -> tuple.get(wishCompany.member.id), Collectors.toList())
            ));
    }

    private List<Tuple> fetchWishCompanyInfos(Long memberId, Long cursor, int size) {
        return queryFactory
            .select(wishCompany.id, company.id, company.name, company.logoUrl)
            .from(wishCompany)
            .join(wishCompany.company, company)
            .where(
                wishCompany.member.id.eq(memberId),
                cursorCondition(cursor)
            )
            .groupBy(company.id, company.name, company.logoUrl)
            .orderBy(wishCompany.id.desc())
            .limit(size + 1)
            .fetch();
    }

    private Map<Long, Long> fetchWishCounts(List<Long> companyIds) {
        return queryFactory
            .select(wishCompany.company.id, wishCompany.count())
            .from(wishCompany)
            .where(wishCompany.company.id.in(companyIds))
            .groupBy(wishCompany.company.id)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(wishCompany.company.id),
                tuple -> tuple.get(wishCompany.count())
            ));
    }

    private List<CompanySummaryInfo.Keyword> fetchCompanyKeywords(Long companyId) {
        return queryFactory
            .select(Projections.constructor(
                CompanySummaryInfo.Keyword.class,
                companyKeyword.content
            ))
            .from(companyKeyword)
            .where(companyKeyword.company.id.eq(companyId))
            .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null
            ? wishCompany.id.lt(cursor)
            : null;
    }
}
