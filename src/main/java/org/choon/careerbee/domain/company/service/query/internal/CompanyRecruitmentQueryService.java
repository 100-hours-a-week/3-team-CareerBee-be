package org.choon.careerbee.domain.company.service.query.internal;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.internal.CompanyRecruitInfo;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyRecruitmentQueryService {

    private final CompanyRepository companyRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Cacheable(cacheNames = "recruitments", key = "#companyId")
    public CompanyRecruitInfo fetchRecruitmentInfo(Long companyId) {
        return companyRepository.fetchRecruitmentInfo(companyId);
    }

    public RecruitingStatus fetchCompanyRecruitStatus(Long companyId) {
        if (recruitmentRepository.existsByCompanyId(companyId)) {
            return RecruitingStatus.ONGOING;
        }

        return RecruitingStatus.CLOSED;
    }
}
