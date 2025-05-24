package org.choon.careerbee.domain.company.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryAddressInfo;
import org.choon.careerbee.domain.company.dto.request.CompanyQueryCond;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDSLConfig.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyCustomRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompanyCustomRepositoryImpl companyCustomRepository;

    @BeforeEach
    void setup() {
        em.persist(createNexon());
        em.persist(createSoop());
        em.persist(createNHN());
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("반경 내 기업 3개가 정상적으로 조회되는가")
    void fetchByDistanceAndCondition_반경내_여러기업조회() {
        // given
        CompanyQueryAddressInfo addressInfo = new CompanyQueryAddressInfo(37.40024430415324, 127.10698761648364);
        CompanyQueryCond cond = new CompanyQueryCond(1000); // 500m

        // when
        CompanyRangeSearchResp result = companyCustomRepository.fetchByDistanceAndCondition(addressInfo, cond);

        // then
        assertThat(result.companies()).hasSize(3);
    }

    private Company createNexon() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(127.1034665, 37.40203443));
        return new Company(
            null, "넥슨코리아", geoPoint,
            "경기 성남시 분당구 판교로256번길 7",
            "http://company.nexon.com/",
            "https://board.jinhak.com/BoardV1/Upload/Job/Company/CI/381798.jpg",
            "국내 대표 게임 개발사",
            "재무평가_상위10%, 주요계열사, 매출1조이상",
            "...",
            CompanyType.ENTERPRISE,
            RecruitingStatus.ONGOING,
            BusinessType.GAME,
            "...", null, 800, 3699, 61510000, 0, 662100000000L, 0L,
            "...", 4.2, Map.of()
        );
    }

    private Company createSoop() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(127.1009721, 37.40122945));
        return new Company(
            null, "SOOP", geoPoint,
            "경기 성남시 분당구 판교로228번길 15",
            "http://corp.sooplive.co.kr/",
            "https://board.jinhak.com/BoardV1/Upload/Job/Company/CI/619914.jpg",
            "지속가능한 IT 솔루션 제공사",
            "재무평가_상위1%, TOP중견",
            "...",
            CompanyType.MID_SIZED, RecruitingStatus.ONGOING,
            BusinessType.PLATFORM,
            "...", null, 600, 805, 41050000, 0, 413200000000L, 113500000000L,
            "...", 4.0, Map.of()
        );
    }

    private Company createNHN() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point geoPoint = factory.createPoint(new Coordinate(127.104216, 37.40102842));
        return new Company(
            null, "NHN", geoPoint,
            "경기 성남시 분당구 대왕판교로645번길 16",
            "http://www.nhn.com/",
            "https://board.jinhak.com/BoardV1/Upload/Job/Company/CI/J84229.jpg",
            "다양한 IT 서비스 제공 기업",
            "재무평가_상위3%, TOP중견",
            "...",
            CompanyType.ENTERPRISE, RecruitingStatus.ONGOING,
            BusinessType.GAME,
            "...", null, 600, 892, 81370000, 46040000, 2456100000000L, 32600000000L,
            "...", 4.1, Map.of()
        );
    }

}