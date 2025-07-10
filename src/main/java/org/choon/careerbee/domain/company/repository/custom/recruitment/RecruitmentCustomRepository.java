package org.choon.careerbee.domain.company.repository.custom.recruitment;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.choon.careerbee.domain.company.dto.response.CompanyActiveCount;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;

public interface RecruitmentCustomRepository {

    Collection<Long> findRecruitingIdByRecruitingIdIn(List<Long> jobIds);

    List<Recruitment> findExpiredBefore(LocalDateTime now);

    List<CompanyActiveCount> countActiveByCompany();
}
