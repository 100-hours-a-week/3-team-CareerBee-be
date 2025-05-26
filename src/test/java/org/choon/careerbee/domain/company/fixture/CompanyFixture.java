package org.choon.careerbee.domain.company.fixture;

import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class CompanyFixture {

    public static Company createCompany(String name, double latitude, double longitude) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(longitude, latitude));
        return Company.builder()
            .name(name)
            .geoPoint(geoPoint)
            .address("서울시 강남구")
            .logoUrl("https://logo.test.com")
            .description("테스트 기업 설명")
            .recentIssue("테스트 이슈")
            .companyType(CompanyType.MID_SIZED)
            .recruitingStatus(RecruitingStatus.ONGOING)
            .businessType(BusinessType.PLATFORM)
            .score(100)
            .employeeCount(50)
            .annualSalary(60000)
            .startingSalary(40000)
            .revenue(1000000000L)
            .operatingProfit(200000000L)
            .ir("IR 정보")
            .rating(4.5)
            .benefits(null)
            .build();
    }
}
