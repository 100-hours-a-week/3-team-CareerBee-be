package org.choon.careerbee.domain.competition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "competition_summary",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "type", "period_start", "period_end"})
    })
public class CompetitionSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "solved_count", nullable = false)
    private Short solvedCount;

    @Column(name = "elapsed_time", nullable = false)
    private Long elapsedTime;

    @Column(nullable = false)
    private Long ranking;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private SummaryType type;

    @Column(name = "max_continuous_day", nullable = false)
    private Integer maxContinuousDays;

    @Column(name = "correct_rate", nullable = false)
    private Double correctRate;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Builder
    private CompetitionSummary(
        Member member, Short solvedCount, Long elapsedTime,
        Long ranking, Integer maxContinuousDays, Double correctRate,
        SummaryType type, LocalDate periodStart, LocalDate periodEnd
    ) {
        this.member = member;
        this.solvedCount = solvedCount;
        this.elapsedTime = elapsedTime;
        this.ranking = ranking;
        this.type = type;
        this.maxContinuousDays = maxContinuousDays;
        this.correctRate = correctRate;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public static CompetitionSummary of(
        Member member, Short solvedCount, Long elapsedTime,
        Long ranking, Integer maxContinuousDays, Double correctRate,
        SummaryType type, LocalDate periodStart, LocalDate periodEnd
    ) {
        return CompetitionSummary.builder()
            .member(member)
            .solvedCount(solvedCount)
            .elapsedTime(elapsedTime)
            .ranking(ranking)
            .type(type)
            .maxContinuousDays(maxContinuousDays)
            .correctRate(correctRate)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .build();
    }
}
