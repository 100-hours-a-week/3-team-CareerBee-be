package org.choon.careerbee.domain.company.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.domain.company.entity.enums.BusinessType;
import org.choon.careerbee.domain.company.entity.enums.CompanyType;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "POINT SRID 4326")
    private Point geoPoint;

    @Column(length = 200)
    private String address;

    @Column(length = 500, unique = true)
    private String homeUrl;

    @Column(length = 500)
    private String markerUrl;

    @Column(length = 250)
    private String description;

    @Column(length = 50)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String recentIssue;

    @Column(length = 14, nullable = false)
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    @Column(length = 7, nullable = false)
    @Enumerated(EnumType.STRING)
    private RecruitingStatus recruitingStatus;

    @Column(length = 8, nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 500, nullable = false)
    private String logoUrl;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer employeeCount;

    @Column(nullable = false)
    private Integer annualSalary;

    @Column(nullable = false)
    private Integer startingSalary;

    @Column(nullable = false)
    private Long revenue;

    @Column(nullable = false)
    private Long operatingProfit;

    @Column(columnDefinition = "LONGTEXT")
    private String ir;

    private Double rating;

    @Type(JsonType.class)
    @Column(name = "benefits", columnDefinition = "longtext")
    private Map<String, List<String>> benefits = new HashMap<>();
}