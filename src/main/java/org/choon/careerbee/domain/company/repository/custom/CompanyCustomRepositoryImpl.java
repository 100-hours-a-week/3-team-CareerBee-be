package org.choon.careerbee.domain.company.repository.custom;

import static org.choon.careerbee.domain.company.entity.QCompany.company;
import static org.choon.careerbee.domain.company.entity.QCompanyKeyword.companyKeyword;
import static org.choon.careerbee.domain.company.entity.QCompanyPhoto.companyPhoto;
import static org.choon.careerbee.domain.company.entity.recruitment.QRecruitment.recruitment;
import static org.choon.careerbee.domain.company.entity.techStack.QCompanyTechStack.companyTechStack;
import static org.choon.careerbee.domain.company.entity.techStack.QTechStack.techStack;
import static org.choon.careerbee.domain.company.entity.wish.QWishCompany.wishCompany;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.dto.internal.CompanySummaryInfoWithoutWish;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyDetailResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.LocationInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp;
import org.choon.careerbee.domain.company.dto.response.CompanySearchResp.CompanySearchInfo;
import org.choon.careerbee.domain.company.dto.response.CompanySummaryInfo;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CompanyCustomRepositoryImpl implements CompanyCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CompanyRangeSearchResp fetchByDistanceAndCondition(
        CompanyQueryAddressInfo companyQueryAddressInfo, CompanyQueryCond companyQueryCond) {
        List<CompanyMarkerInfo> results = queryFactory
            .select(Projections.constructor(
                CompanyMarkerInfo.class,
                company.id,
                company.markerUrl,
                company.businessType,
                company.recruitingStatus,
                Projections.constructor(
                    CompanyRangeSearchResp.LocationInfo.class,
                    Expressions.numberTemplate(Double.class, "ST_X({0})", company.geoPoint),
                    Expressions.numberTemplate(Double.class, "ST_Y({0})", company.geoPoint)
                )
            ))
            .from(company)
            .where(
                inDistance(companyQueryAddressInfo.toWKTPoint(), companyQueryCond.radius()),
                recruitingStatusEq(companyQueryCond.recruitingStatus()),
                businessTypeEq(companyQueryCond.type())
            )
            .fetch();

        return new CompanyRangeSearchResp(results);
    }

    @Override
    public CompanySummaryInfo fetchCompanySummaryInfoById(Long companyId) {
        Tuple tuple = queryFactory
            .select(
                company.id,
                company.name,
                company.logoUrl
            )
            .from(company)
            .where(company.id.eq(companyId))
            .fetchOne();

        if (tuple == null) {
            throw new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST);
        }

        List<CompanySummaryInfo.Keyword> keywords = queryFactory
            .select(Projections.constructor(
                CompanySummaryInfo.Keyword.class,
                companyKeyword.content
            ))
            .from(companyKeyword)
            .where(companyKeyword.company.id.eq(companyId))
            .fetch();

        return new CompanySummaryInfo(
            tuple.get(company.id),
            tuple.get(company.name),
            tuple.get(company.logoUrl),
            getWishCount(companyId),
            keywords
        );
    }

    @Override
    public CompanySummaryInfoWithoutWish fetchCompanySummaryInfoWithoutWishCount(Long companyId) {
        Tuple tuple = queryFactory
            .select(
                company.id,
                company.name,
                company.logoUrl
            )
            .from(company)
            .where(company.id.eq(companyId))
            .fetchOne();

        if (tuple == null) {
            throw new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST);
        }

        List<CompanySummaryInfo.Keyword> keywords = queryFactory
            .select(Projections.constructor(
                CompanySummaryInfo.Keyword.class,
                companyKeyword.content
            ))
            .from(companyKeyword)
            .where(companyKeyword.company.id.eq(companyId))
            .fetch();

        return new CompanySummaryInfoWithoutWish(
            tuple.get(company.id),
            tuple.get(company.name),
            tuple.get(company.logoUrl),
            keywords
        );
    }

    @Override
    public Long fetchWishCountById(Long companyId) {
        return queryFactory
            .select(wishCompany.count())
            .from(wishCompany)
            .where(wishCompany.company.id.eq(companyId))
            .fetchOne();
    }

    @Override
    public CompanyDetailResp fetchCompanyDetailById(Long companyId) {
        Company companyEntity = queryFactory
            .selectFrom(company)
            .where(company.id.eq(companyId))
            .fetchOne();

        if (companyEntity == null) {
            throw new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST);
        }

        Long wishCount = getWishCount(companyId);

        List<CompanyDetailResp.Photo> photos = queryFactory
            .select(Projections.constructor(
                CompanyDetailResp.Photo.class,
                companyPhoto.displayOrder,
                companyPhoto.imgUrl
            ))
            .from(companyPhoto)
            .where(companyPhoto.company.id.eq(companyId))
            .orderBy(companyPhoto.displayOrder.asc())
            .fetch();

        List<CompanyDetailResp.TechStack> techStacks = queryFactory
            .select(Projections.constructor(
                CompanyDetailResp.TechStack.class,
                techStack.id,
                techStack.name,
                techStack.stackType.stringValue(),
                techStack.imgUrl
            ))
            .from(companyTechStack)
            .join(companyTechStack.techStack, techStack)
            .where(companyTechStack.company.id.eq(companyId))
            .fetch();

        List<CompanyDetailResp.Recruitment> recruitments = queryFactory
            .select(Projections.constructor(
                CompanyDetailResp.Recruitment.class,
                recruitment.recruitingId,
                recruitment.url,
                recruitment.title,
                recruitment.startDate.stringValue(),
                recruitment.endDate.stringValue()
            ))
            .from(recruitment)
            .where(recruitment.company.id.eq(companyId))
            .orderBy(recruitment.startDate.desc())
            .fetch();

        return new CompanyDetailResp(
            companyEntity.getId(),
            companyEntity.getName(),
            companyEntity.getTitle(),
            companyEntity.getLogoUrl(),
            companyEntity.getRecentIssue(),
            companyEntity.getCompanyType().name(),
            companyEntity.getRecruitingStatus().name(),
            companyEntity.getAddress(),
            companyEntity.getEmployeeCount(),
            companyEntity.getHomeUrl(),
            companyEntity.getDescription(),
            wishCount != null ? wishCount : 0,
            companyEntity.getRating() != null ? companyEntity.getRating() : 0.0,
            new CompanyDetailResp.Financials(
                companyEntity.getAnnualSalary(),
                companyEntity.getStartingSalary(),
                companyEntity.getRevenue(),
                companyEntity.getOperatingProfit()
            ),
            photos,
            CompanyDetailResp.convertBenefitMap(companyEntity.getBenefits()),
            techStacks,
            recruitments
        );
    }

    @Override
    public CompanySearchResp fetchMatchingCompaniesByKeyword(String keyword) {
        List<CompanySearchInfo> result = queryFactory
            .select(
                Projections.constructor(
                    CompanySearchInfo.class,
                    company.id,
                    company.name
                )
            )
            .from(company)
            .where(company.name.like("%" + keyword + "%", '!'))
            .limit(8)
            .fetch();

        return new CompanySearchResp(result);
    }

    @Override
    public CompanyMarkerInfo fetchCompanyMarkerInfo(Long companyId) {
        CompanyMarkerInfo result = queryFactory.select(
                Projections.constructor(
                    CompanyMarkerInfo.class,
                    company.id,
                    company.markerUrl,
                    company.businessType,
                    company.recruitingStatus,
                    Projections.constructor(
                        LocationInfo.class,
                        Expressions.numberTemplate(Double.class, "ST_X({0})", company.geoPoint),
                        Expressions.numberTemplate(Double.class, "ST_Y({0})", company.geoPoint)
                    )
                )
            )
            .from(company)
            .where(company.id.eq(companyId))
            .fetchOne();

        if (result == null) {
            throw new CustomException(CustomResponseStatus.COMPANY_NOT_EXIST);
        }

        return result;
    }

    @Override
    public List<Company> findBySaraminNameIn(List<String> companyNames) {
        if (companyNames == null || companyNames.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
            .selectFrom(company)
            .where(company.saraminName.in(companyNames))
            .fetch();
    }

    @Override
    public List<CompanyMarkerInfo> fetchAllCompanyMarkerInfo() {
        return queryFactory.select(
                Projections.constructor(
                    CompanyMarkerInfo.class,
                    company.id,
                    company.markerUrl,
                    company.businessType,
                    company.recruitingStatus,
                    Projections.constructor(
                        LocationInfo.class,
                        Expressions.numberTemplate(Double.class, "ST_X({0})", company.geoPoint),
                        Expressions.numberTemplate(Double.class, "ST_Y({0})", company.geoPoint)
                    )
                )
            )
            .from(company)
            .fetch();
    }

    private BooleanExpression inDistance(String point, Integer radius) {
        return radius != null
            ? Expressions.booleanTemplate(
            "ST_Distance_Sphere(ST_GeomFromText({0}, 4326), {1}) <= {2}",
            point, company.geoPoint, radius)
            : null;
    }

    private BooleanExpression recruitingStatusEq(RecruitingStatus recruitingStatus) {
        if (recruitingStatus == null) {
            return null;
        }

        return company.recruitingStatus.eq(recruitingStatus);
    }

    private BooleanExpression businessTypeEq(BusinessType businessType) {
        if (businessType == null) {
            return null;
        }

        return company.businessType.eq(businessType);
    }

    private Long getWishCount(Long companyId) {
        return queryFactory
            .select(wishCompany.count())
            .from(wishCompany)
            .where(wishCompany.company.id.eq(companyId))
            .fetchOne();
    }

}
