package org.choon.careerbee.domain.company.schedule;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyScheduleRunner {

    private static final List<String> KEYWORDS = List.of(
        "it", "백엔드", "프론트엔드", "devops", "솔루션", "si", "보안"
    );

    private final CompanyCommandService commandService;

    @Scheduled(cron = "0 0 14 * * *") // 매일 오후 2시에 한 번 실행
    public void updateRecruiting() {
        for (String keyword : KEYWORDS) {
            commandService.updateCompanyRecruiting(keyword);
            commandService.updateCompanyOpenRecruiting(keyword);
        }
    }

}
