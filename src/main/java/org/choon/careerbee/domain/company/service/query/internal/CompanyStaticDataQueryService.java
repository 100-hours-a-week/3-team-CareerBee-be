package org.choon.careerbee.domain.company.service.query.internal;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.internal.CompanyStaticPart;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyStaticDataQueryService {

    private final CompanyRepository companyRepository;

    @Cacheable(cacheNames = "companyStaticDetail", key = "#companyId", unless = "#result == null")
    public CompanyStaticPart fetchCompanyStaticPart(Long companyId) {
        return companyRepository.fetchCompanyStaticInfoById(
            companyId);
    }
}
