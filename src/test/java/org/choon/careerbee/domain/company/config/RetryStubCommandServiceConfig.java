package org.choon.careerbee.domain.company.config;


import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.RecruitmentSyncService;
import org.choon.careerbee.domain.company.service.RetryStubCompanyCommandService;
import org.choon.careerbee.domain.company.service.command.CompanyCommandService;
import org.choon.careerbee.domain.company.service.query.CompanyQueryService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RetryStubCommandServiceConfig {

    @Bean
    public CompanyCommandService companyCommandService(
        RecruitmentRepository recruitmentRepository,
        CompanyApiClient companyApiClient,
        WishCompanyRepository wishCompanyRepository,
        MemberQueryService memberQueryService,
        CompanyQueryService companyQueryService,
        RecruitmentSyncService recruitmentSyncService,
        RedissonClient redissonClient
    ) {
        return new RetryStubCompanyCommandService(
            recruitmentRepository,
            companyApiClient,
            wishCompanyRepository,
            memberQueryService,
            companyQueryService,
            recruitmentSyncService,
            redissonClient
        );
    }
}
