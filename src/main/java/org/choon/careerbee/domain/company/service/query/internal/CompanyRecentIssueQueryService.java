package org.choon.careerbee.domain.company.service.query.internal;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyRecentIssueQueryService {

    private final CompanyRepository companyRepository;

    @Cacheable(cacheNames = "recentIssue", key = "#companyId")
    public String fetchRecentIssue(Long companyId) {
        return companyRepository.fetchCompanyRecentIssueById(companyId);
    }
}
