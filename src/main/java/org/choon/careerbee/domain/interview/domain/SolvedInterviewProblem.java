package org.choon.careerbee.domain.interview.domain;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.interview.domain.enums.SaveStatus;
import org.choon.careerbee.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "solved_interview_problem",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "interview_problem_id"})
    })
public class SolvedInterviewProblem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_problem_id", nullable = false)
    private InterviewProblem interviewProblem;

    @Column(length = 500, nullable = false)
    private String answer;

    @Column(length = 500, nullable = false)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "save_status", nullable = false)
    private SaveStatus saveStatus;

    private SolvedInterviewProblem(
        Member member, InterviewProblem interviewProblem, String answer,
        String feedback, SaveStatus saveStatus
    ) {
        this.member = member;
        this.interviewProblem = interviewProblem;
        this.answer = answer;
        this.feedback = feedback;
        this.saveStatus = saveStatus;
    }

    @Builder
    public static SolvedInterviewProblem of(
        Member member, InterviewProblem interviewProblem, String answer,
        String feedback, SaveStatus saveStatus
    ) {
        return new SolvedInterviewProblem(
            member, interviewProblem, answer, feedback, saveStatus
        );
    }

    public void save() {
        if (this.saveStatus.equals(SaveStatus.SAVED)) {
            throw new CustomException(CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_SAVED);
        }

        this.saveStatus = SaveStatus.SAVED;
    }

    public void cancelSave() {
        if (this.saveStatus.equals(SaveStatus.UNSAVED)) {
            throw new CustomException(CustomResponseStatus.INTERVIEW_PROBLEM_ALREADY_UNSAVED);
        }

        this.saveStatus = SaveStatus.UNSAVED;
    }
}
