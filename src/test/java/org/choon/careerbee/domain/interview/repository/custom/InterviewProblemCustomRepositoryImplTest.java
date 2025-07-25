package org.choon.careerbee.domain.interview.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.interview.InterviewProblemFixture.createInterviewProblem;

import java.util.List;
import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;
import org.choon.careerbee.domain.interview.repository.InterviewProblemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class InterviewProblemCustomRepositoryTest {

    @Autowired
    private InterviewProblemRepository interviewProblemRepository;

    @Autowired
    private InterviewProblemCustomRepositoryImpl customRepository;

    @Test
    @DisplayName("각 ProblemType 별 가장 먼저 저장된 InterviewProblem을 1개씩 조회한다")
    void fetchFirstInterviewProblemsByType_success() {
        // given
        interviewProblemRepository.save(createInterviewProblem("백엔드 문제 1", ProblemType.BACKEND));
        interviewProblemRepository.save(createInterviewProblem("백엔드 문제 2", ProblemType.BACKEND));
        interviewProblemRepository.save(createInterviewProblem("프론트엔드 문제 1", ProblemType.FRONTEND));
        interviewProblemRepository.save(createInterviewProblem("AI 문제 1", ProblemType.AI));
        interviewProblemRepository.save(createInterviewProblem("AI 문제 2", ProblemType.AI));
        interviewProblemRepository.save(createInterviewProblem("DevOps 문제 1", ProblemType.DEVOPS));

        // when
        List<InterviewProblemInfo> result = customRepository.fetchFirstInterviewProblemsByType();

        // then
        assertThat(result).hasSize(4);

        assertThat(result).anySatisfy(info -> {
            assertThat(info.type()).isEqualTo(ProblemType.BACKEND);
            assertThat(info.question()).isEqualTo("백엔드 문제 1");
        });

        assertThat(result).anySatisfy(info -> {
            assertThat(info.type()).isEqualTo(ProblemType.FRONTEND);
            assertThat(info.question()).isEqualTo("프론트엔드 문제 1");
        });

        assertThat(result).anySatisfy(info -> {
            assertThat(info.type()).isEqualTo(ProblemType.AI);
            assertThat(info.question()).isEqualTo("AI 문제 1");
        });

        assertThat(result).anySatisfy(info -> {
            assertThat(info.type()).isEqualTo(ProblemType.DEVOPS);
            assertThat(info.question()).isEqualTo("DevOps 문제 1");
        });
    }
}
