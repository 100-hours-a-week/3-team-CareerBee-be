package org.choon.careerbee.domain.company.repository.custom;

import static org.choon.careerbee.domain.company.entity.QCompany.*;
import static org.choon.careerbee.domain.company.entity.QCompanyKeyword.companyKeyword;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanySummary;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.QCompany;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompanyCustomRepositoryImpl implements CompanyCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CompanyRangeSearchResp fetchByDistanceAndCondition(
      CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond) {

    List<CompanySummary> results = queryFactory
        .select(Projections.constructor(
            CompanyRangeSearchResp.CompanySummary.class,
            company.id,
            company.logoUrl,
            Projections.constructor(
                CompanyRangeSearchResp.LocationInfo.class,
                Expressions.numberTemplate(Double.class, "ST_X({0})", company.geoPoint),
                Expressions.numberTemplate(Double.class, "ST_Y({0})", company.geoPoint)
            )
        ))
        .from(company)
        .where(
            inDistance(companyQueryAddressInfo.toWKTPoint(), companyQueryCond.radius())
        )
        .fetch();

    return new CompanyRangeSearchResp(results);
  }

  @Override
  public CompanySummaryInfo fetchCompanySummaryInfoById(Long companyId) {
    CompanySummaryInfo baseInfo = queryFactory
        .select(Projections.constructor(
            CompanySummaryInfo.class,
            company.id,
            company.name,
            company.logoUrl,
            Expressions.constant(Collections.emptyList())
        ))
        .from(company)
        .where(company.id.eq(companyId))
        .fetchOne();

    List<CompanySummaryInfo.Keyword> keywords = queryFactory
        .select(Projections.constructor(
            CompanySummaryInfo.Keyword.class,
            companyKeyword.content
        ))
        .from(companyKeyword)
        .where(companyKeyword.company.id.eq(companyId))
        .fetch();

    return new CompanySummaryInfo(
        baseInfo.id(),
        baseInfo.name(),
        baseInfo.logoUrl(),
        keywords
    );
  }

  private BooleanExpression inDistance(String point, Integer radius) {
    return radius != null
        ? Expressions.booleanTemplate(
        "ST_Distance_Sphere(ST_GeomFromText({0}, 4326), {1}) <= {2}",
        point, company.geoPoint, radius)
        : null;
  }
}
