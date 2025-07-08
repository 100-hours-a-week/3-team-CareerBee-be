package org.choon.careerbee.domain.interview.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "interview_problem")
public class InterviewProblem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    private ProblemType type;

    private InterviewProblem(String question, ProblemType type) {
        this.question = question;
        this.type = type;
    }

    @Builder
    public static InterviewProblem of(String question, ProblemType type) {
        return new InterviewProblem(question, type);
    }
}
