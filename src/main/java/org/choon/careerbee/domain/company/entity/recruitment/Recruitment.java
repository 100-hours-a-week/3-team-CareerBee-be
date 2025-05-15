package org.choon.careerbee.domain.company.entity.recruitment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.domain.company.entity.Company;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "recruitment")
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, unique = true)
    private Long recruitingId;

    @Column(length = 500)
    private String url;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Builder
    private Recruitment(Company company, Long recruitingId, String url, String title,
        LocalDateTime startDate, LocalDateTime endDate) {
        this.company = company;
        this.recruitingId = recruitingId;
        this.url = url;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Recruitment from(Company company, Long recruitingId, String url, String title,
        LocalDateTime startDate, LocalDateTime endDate) {
        return Recruitment.builder()
            .company(company)
            .recruitingId(recruitingId)
            .url(url)
            .title(title)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}