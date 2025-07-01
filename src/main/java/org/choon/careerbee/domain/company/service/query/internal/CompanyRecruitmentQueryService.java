package org.choon.careerbee.domain.company.service.query.internal;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyRecruitmentQueryService {

    private final CompanyRepository companyRepository;

    @Cacheable(cacheNames = "recruitments", key = "#companyId")
    public CompanyRecruitInfo fetchRecruitmentInfo(Long companyId) {
        return companyRepository.fetchRecruitmentInfo(companyId);
    }
}
