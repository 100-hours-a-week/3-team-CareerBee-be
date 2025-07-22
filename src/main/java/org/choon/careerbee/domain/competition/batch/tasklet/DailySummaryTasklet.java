package org.choon.careerbee.domain.competition.batch.tasklet;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.service.summary.CompetitionSummaryService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class DailySummaryTasklet implements Tasklet {

    @Qualifier("dbRetryTemplate")
    private final RetryTemplate retryTemplate;

    @Value("#{T(java.time.LocalDate).parse(jobParameters['date'])}")
    private LocalDate jobDate;

    private final CompetitionSummaryService summaryService;

    @Override
    public RepeatStatus execute(
        StepContribution contribution, ChunkContext chunkContext
    ) {
        StepExecution stepExecution = contribution.getStepExecution();
        retryTemplate.execute(cx -> {
            log.info("[{}] DailySummaryTasklet start", jobDate);
            cx.setAttribute("stepExecution", stepExecution);
            summaryService.dailySummary(jobDate);
            return null;
        });
        return RepeatStatus.FINISHED;
    }
}
