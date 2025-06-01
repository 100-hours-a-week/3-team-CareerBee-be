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
import org.choon.careerbee.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemChoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_problem_id", nullable = false)
    private CompetitionProblem competitionProblem;

    @Column(length = 100, nullable = false)
    private String content;

    @Column(name = "choice_order", nullable = false)
    private Short choiceOrder;

    @Builder
    private ProblemChoice(CompetitionProblem competitionProblem, String content, Short choiceOrder) {
        this.competitionProblem = competitionProblem;
        this.content = content;
        this.choiceOrder = choiceOrder;
    }

    public static ProblemChoice of(CompetitionProblem competitionProblem, String content, Short choiceOrder) {
        return ProblemChoice.builder()
            .competitionProblem(competitionProblem)
            .content(content)
            .choiceOrder(choiceOrder)
            .build();
    }
}
