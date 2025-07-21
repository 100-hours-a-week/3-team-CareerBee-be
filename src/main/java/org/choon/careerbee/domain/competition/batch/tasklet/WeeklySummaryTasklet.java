package org.choon.careerbee.domain.competition.batch.tasklet;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.service.summary.CompetitionSummaryService;
import org.choon.careerbee.util.date.DateUtil;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeeklySummaryTasklet implements Tasklet {

    private final CompetitionSummaryService summaryService;

    @Value("#{T(java.time.LocalDate).parse(jobParameters['date'])}")
    private LocalDate date;

    @Override
    public RepeatStatus execute(StepContribution contrib, ChunkContext ctx) {
        log.info("[{}] WeeklySummaryTasklet start", date);
        summaryService.weekAndMonthSummary(
            DateUtil.getPeriod(date, SummaryType.WEEK), SummaryType.WEEK);
        return RepeatStatus.FINISHED;
    }
}
