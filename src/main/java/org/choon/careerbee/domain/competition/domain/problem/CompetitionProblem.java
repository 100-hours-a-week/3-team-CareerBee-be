package org.choon.careerbee.domain.competition.domain.problem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.domain.competition.domain.Competition;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompetitionProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 200, nullable = false)
    private String description;

    @Column(length = 300, nullable = false)
    private String solution;

    @Column(nullable = false)
    private Short answer;

    @Builder
    private CompetitionProblem(
        Competition competition, String title, String description,
        String solution, Short answer
    ) {
        this.competition = competition;
        this.title = title;
        this.description = description;
        this.solution = solution;
        this.answer = answer;
    }

    public static CompetitionProblem of(
        Competition competition, String title, String description,
        String solution, Short answer
    ) {
        return CompetitionProblem.builder()
            .competition(competition)
            .title(title)
            .description(description)
            .solution(solution)
            .answer(answer)
            .build();
    }
}
