package org.choon.careerbee.domain.competition.runner;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.service.query.CompetitionQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompetitionCachingRunner {

    private final CompetitionQueryService queryService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void competitionIdCacheWarmUpJob() {
        log.info("오늘 대회 ID 캐시 웜업 시작");
        CompetitionIdResp competitionIdResp = queryService.fetchCompetitionIdBy(LocalDate.now());
        log.info("오늘 대회 ID 캐시 웜업 완료 - 대회 ID : {}", competitionIdResp);
    }

    @Scheduled(cron = "0 58 12 * * *", zone = "Asia/Seoul")
    public void competitionProblemCacheWarmUpJob() {
        CompetitionIdResp competitionIdResp = queryService.fetchCompetitionIdBy(LocalDate.now());
        log.info("오늘 대회 문제 캐시 웜업 시작 : {}", competitionIdResp);
        queryService.fetchProblems(competitionIdResp.competitionId());
        log.info("오늘 대회 문제 캐시 웜업 완료 : {}", competitionIdResp);
    }
}
