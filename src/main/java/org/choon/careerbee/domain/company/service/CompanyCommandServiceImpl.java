package org.choon.careerbee.domain.company.service;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {
    private final CompanyApiClient companyApiClient;

    private final WishCompanyRepository wishCompanyRepository;
    private final MemberQueryService memberQueryService;
    private final CompanyQueryService companyQueryService;
    private final RecruitmentSyncService recruitmentSyncService;

    @Override
    public void registWishCompany(Long accessMemberId, Long companyId) {
        Member validMember = memberQueryService.findById(accessMemberId);
        Company validCompany = companyQueryService.findById(companyId);

        if (wishCompanyRepository.existsByMemberAndCompany(validMember, validCompany)) {
            throw new CustomException(CustomResponseStatus.WISH_ALREADY_EXIST);
        }

        wishCompanyRepository.save(WishCompany.of(validMember, validCompany));
    }

    @Override
    public void deleteWishCompany(Long accessMemberId, Long companyId) {
        Member validMember = memberQueryService.findById(accessMemberId);
        Company validCompany = companyQueryService.findById(companyId);

        WishCompany wishCompany = wishCompanyRepository
            .findByMemberAndCompany(validMember, validCompany)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.WISH_COMPANY_NOT_FOUND));

        wishCompanyRepository.delete(wishCompany);
    }

    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void updateCompanyRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchAllRecruitment(keyword);
        log.info("전체 공고 개수 : {}", apiResp.jobs().job().size());

        recruitmentSyncService.persistNewRecruitmentsAndNotify(apiResp, false);
    }

    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void updateCompanyOpenRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchOpenRecruitment(keyword);
        log.info("공채 공고 개수 : {}", apiResp.jobs().job().size());

        recruitmentSyncService.persistNewRecruitmentsAndNotify(apiResp, true);
    }

    @Recover
    public void recruitingRecover(RestClientException ex, String keyword) {
        log.error("[Saramin API No React] {}에 대한 미응담", keyword);
        Sentry.captureException(ex);
    }

}
