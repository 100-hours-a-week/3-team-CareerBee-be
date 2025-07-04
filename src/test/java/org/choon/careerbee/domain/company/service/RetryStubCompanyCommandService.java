package org.choon.careerbee.domain.company.service;

import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.exception.RetryableSaraminException;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.command.CompanyCommandServiceImpl;
import org.choon.careerbee.domain.company.service.query.CompanyQueryService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.redisson.api.RedissonClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public class RetryStubCompanyCommandService extends CompanyCommandServiceImpl {

    public RetryStubCompanyCommandService(
        RecruitmentRepository recruitmentRepository,
        CompanyApiClient companyApiClient,
        WishCompanyRepository wishCompanyRepository,
        MemberQueryService memberQueryService,
        CompanyQueryService companyQueryService,
        RecruitmentSyncService recruitmentSyncService,
        RedissonClient redissonClient
    ) {
        super(
            recruitmentRepository,
            companyApiClient,
            wishCompanyRepository,
            memberQueryService,
            companyQueryService,
            recruitmentSyncService,
            redissonClient
        );
    }

    @Override
    @Retryable(
        retryFor = RetryableSaraminException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1)
    )
    public void updateCompanyRecruiting(String keyword) {
        super.updateCompanyRecruiting(keyword);
    }

    @Override
    @Retryable(
        retryFor = RetryableSaraminException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1)
    )
    public void updateCompanyOpenRecruiting(String keyword) {
        super.updateCompanyOpenRecruiting(keyword);
    }

    @Override
    public void recruitingRecover(RetryableSaraminException ex, String keyword) {
        super.recruitingRecover(ex, keyword);
    }
}
