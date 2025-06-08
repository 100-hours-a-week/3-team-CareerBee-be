package org.choon.careerbee.domain.competition.runner;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.service.summary.CompetitionSummaryService;
import org.choon.careerbee.util.date.DateUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompetitionSummaryRunner {

    private final CompetitionSummaryService summaryService;

    @Scheduled(cron = "0 0 14 * * *", zone = "Asia/Seoul")
    public void runCompetitionSummaryJob() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));

        log.info("일일 집계 시작! : {}", now);
        summaryService.dailySummary(now);
        log.info("일일 집계 끝!", now);

        SummaryPeriod weekPeriod = DateUtil.getPeriod(now, SummaryType.WEEK);
        SummaryPeriod monthPeriod = DateUtil.getPeriod(now, SummaryType.MONTH);
        summaryService.weekAndMonthSummary(weekPeriod, SummaryType.WEEK);
        summaryService.weekAndMonthSummary(monthPeriod, SummaryType.MONTH);
    }
}
