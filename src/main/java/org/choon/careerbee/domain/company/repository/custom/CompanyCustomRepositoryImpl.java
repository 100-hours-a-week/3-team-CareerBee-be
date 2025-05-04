package org.choon.careerbee.domain.company.repository.custom;

import static org.choon.careerbee.domain.company.entity.QCompany.*;
import static org.choon.careerbee.domain.company.entity.QCompanyKeyword.companyKeyword;
import static org.choon.careerbee.domain.company.entity.QCompanyPhoto.companyPhoto;
import static org.choon.careerbee.domain.company.entity.QCompanyRating.companyRating;
import static org.choon.careerbee.domain.company.entity.recruitment.QRecruitment.recruitment;
import static org.choon.careerbee.domain.company.entity.techStack.QCompanyTechStack.companyTechStack;
import static org.choon.careerbee.domain.company.entity.techStack.QTechStack.techStack;
import static org.choon.careerbee.domain.company.entity.wish.QWishCompany.wishCompany;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanySummary;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.QCompany;
import org.choon.careerbee.domain.company.entity.wish.QWishCompany;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
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
    return null;
  }


  private BooleanExpression inDistance(String point, Integer radius) {
    return radius != null
        ? Expressions.booleanTemplate(
        "ST_Distance_Sphere(ST_GeomFromText({0}, 4326), {1}) <= {2}",
        point, company.geoPoint, radius)
        : null;
  }
}
