package org.choon.careerbee.domain.company.config;


import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.api.NextApiClient;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
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
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RetryStubCommandServiceConfig {

    @Bean
    @Primary
    public CompanyCommandService companyCommandService(
        RecruitmentRepository recruitmentRepository,
        CompanyRepository companyRepository,
        CompanyApiClient companyApiClient,
        NextApiClient nextApiClient,
        WishCompanyRepository wishCompanyRepository,
        MemberQueryService memberQueryService,
        CompanyQueryService companyQueryService,
        RecruitmentSyncService recruitmentSyncService,
        RedissonClient redissonClient
    ) {
        return new RetryStubCompanyCommandService(
            recruitmentRepository,
            companyRepository,
            companyApiClient,
            nextApiClient,
            wishCompanyRepository,
            memberQueryService,
            companyQueryService,
            recruitmentSyncService,
            redissonClient
        );
    }
}
