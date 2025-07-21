package org.choon.careerbee.domain.competition.batch.job;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.config.batch.JobLoggingListener;
import org.choon.careerbee.domain.competition.batch.tasklet.DailySummaryTasklet;
import org.choon.careerbee.domain.competition.batch.tasklet.MonthlySummaryTasklet;
import org.choon.careerbee.domain.competition.batch.tasklet.WeeklySummaryTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CompetitionSummaryJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLoggingListener loggingListener;

    private final DailySummaryTasklet dailyTasklet;
    private final WeeklySummaryTasklet weeklyTasklet;
    private final MonthlySummaryTasklet monthlyTasklet;

    @Bean
    public Step dailySummaryStep() {
        return new StepBuilder("dailySummaryStep", jobRepository)
            .tasklet(dailyTasklet, transactionManager)
            .faultTolerant()
            .retry(TransientDataAccessException.class)
            .retryLimit(3)
            .build();
    }

    @Bean
    public Step weeklySummaryStep() {
        return new StepBuilder("weeklySummaryStep", jobRepository)
            .tasklet(weeklyTasklet, transactionManager)
            .faultTolerant()
            .retry(TransientDataAccessException.class)
            .retryLimit(3)
            .build();
    }

    @Bean
    public Step monthlySummaryStep() {
        return new StepBuilder("monthlySummaryStep", jobRepository)
            .tasklet(monthlyTasklet, transactionManager)
            .faultTolerant()
            .retry(TransientDataAccessException.class)
            .retryLimit(3)
            .build();
    }

}
