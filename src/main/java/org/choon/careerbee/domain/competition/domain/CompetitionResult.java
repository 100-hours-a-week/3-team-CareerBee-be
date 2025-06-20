package org.choon.careerbee.domain.competition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "competition_result",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "competition_id"})
    })
public class CompetitionResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "solved_count", nullable = false)
    private Short solvedCount;

    @Column(name = "elapsed_time", nullable = false)
    private Integer elapsedTime;

    @Builder
    private CompetitionResult(Competition competition, Member member, Short solvedCount,
        Integer elapsedTime) {
        this.competition = competition;
        this.member = member;
        this.solvedCount = solvedCount;
        this.elapsedTime = elapsedTime;
    }

    public static CompetitionResult of(
        Competition competition, Member member,
        short solvedCount, Integer elapsedTime
    ) {
        return CompetitionResult.builder()
            .competition(competition)
            .member(member)
            .solvedCount(solvedCount)
            .elapsedTime(elapsedTime)
            .build();
    }
}
