package org.choon.careerbee.domain.company.schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.service.command.CompanyCommandService;
import org.choon.careerbee.util.lock.DistributedLockExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompanyScheduleRunner {

    private static final List<String> KEYWORDS = List.of(
        "it", "백엔드", "프론트엔드", "devops", "솔루션", "si", "보안"
    );

    private final CompanyCommandService commandService;
    private final DistributedLockExecutor lockExecutor;

    @Scheduled(cron = "0 0 14 * * *", zone = "Asia/Seoul") // 매일 오후 2시
    public void recruitingScheduleProcess() {
        String lockKey =
            "lock:company:recruiting:" + LocalDateTime.now().toLocalDate(); // 일자 단위로 키 고정

        lockExecutor.execute(lockKey, 3, 600, TimeUnit.SECONDS, () -> {
                LocalDateTime now = LocalDateTime.now();

                log.info("공고 데이터 수집 시작!");
                for (String keyword : KEYWORDS) {
                    commandService.updateCompanyRecruiting(keyword);
                    commandService.updateCompanyOpenRecruiting(keyword);
                }
                log.info("공고 데이터 수집 마감!");

                log.info("[{}] 공고 삭제 및 기업 채용상태 변경 스케줄러 작동", now);
                commandService.cleanExpiredRecruitments(now);
                log.info("[{}] 공고 삭제 및 기업 채용상태 변경 스케줄러 마감", now);
            }
        );
    }

}
