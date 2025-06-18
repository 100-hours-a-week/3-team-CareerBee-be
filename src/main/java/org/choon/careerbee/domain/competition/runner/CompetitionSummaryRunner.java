package org.choon.careerbee.domain.competition.runner;

import io.sentry.Sentry;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.service.summary.CompetitionSummaryService;
import org.choon.careerbee.util.date.DateUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompetitionSummaryRunner {

    private final CompetitionSummaryService summaryService;

    //    @Scheduled(cron = "0 0 14 * * *", zone = "Asia/Seoul")
    // 오후 3시 12분(15:12)에 실행
    @Scheduled(cron = "0 16 13 * * *", zone = "Asia/Seoul")
    public void runCompetitionSummaryJob() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[{}] 대회 결과 데이터 집계 시작!", now);
        Sentry.captureMessage("대회 결과 데이터 집계 시작");
        summaryService.dailySummary(now);

        summaryService.weekAndMonthSummary(DateUtil.getPeriod(now, SummaryType.WEEK),
            SummaryType.WEEK);
        summaryService.weekAndMonthSummary(DateUtil.getPeriod(now, SummaryType.MONTH),
            SummaryType.MONTH);
        log.info("[{}] 대회 결과 데이터 집계 종료!", now);
        Sentry.captureMessage("대회 결과 데이터 집계 종료");
    }
}
