package org.choon.careerbee.domain.company.schedule;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.service.CompanyCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyScheduleRunner {

    private final CompanyCommandService commandService;

    @Scheduled(cron = "0 0 5 * * *")
    public void updateRecruiting() {
        commandService.updateCompanyRecruiting();

        commandService.updateCompanyOpenRecruiting();
    }

}
