package org.choon.careerbee.config.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BatchInfraConfig {

    private final String JOB_NAME = "testJob";
    private final String STEP_NAME = "testStep";

    @Bean
    public Job testJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(testStep(jobRepository, transactionManager))
            .build();
    }

    @Bean
    public Step testStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet(testTasklet(), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet testTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                throws Exception {
                log.info("Spring Batch Test Success");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
