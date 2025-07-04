package org.choon.careerbee.domain.company.service;

import io.sentry.Sentry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.dto.response.CompanyActiveCount;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.exception.RetryableSaraminException;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Profile("!test")
@Transactional
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {

    private final RecruitmentRepository recruitmentRepository;
    private static final long TTL = 3L;

    private final CompanyApiClient companyApiClient;

    private final WishCompanyRepository wishCompanyRepository;
    private final MemberQueryService memberQueryService;
    private final CompanyQueryService companyQueryService;
    private final RecruitmentSyncService recruitmentSyncService;
    private final RedissonClient redissonClient;

    @Override
    public void registWishCompany(Long accessMemberId, Long companyId) {
        Member validMember = memberQueryService.findById(accessMemberId);
        Company validCompany = companyQueryService.findById(companyId);

        String key = "wish:register:" + validMember.getId() + ":" + companyId;
        boolean success = redissonClient.getBucket(key)
            .setIfAbsent("1", Duration.ofSeconds(TTL));

        if (!success) {
            throw new CustomException(CustomResponseStatus.DUPLICATE_REQUEST);
        }

        if (wishCompanyRepository.existsByMemberAndCompany(validMember, validCompany)) {
            throw new CustomException(CustomResponseStatus.WISH_ALREADY_EXIST);
        }

        wishCompanyRepository.save(WishCompany.of(validMember, validCompany));
    }

    @Override
    public void deleteWishCompany(Long accessMemberId, Long companyId) {
        Member validMember = memberQueryService.findById(accessMemberId);
        Company validCompany = companyQueryService.findById(companyId);

        String key = "wish:delete:" + validMember.getId() + ":" + companyId;
        boolean success = redissonClient.getBucket(key)
            .setIfAbsent("1", Duration.ofSeconds(TTL));

        if (!success) {
            throw new CustomException(CustomResponseStatus.DUPLICATE_REQUEST);
        }

        WishCompany wishCompany = wishCompanyRepository
            .findByMemberAndCompany(validMember, validCompany)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.WISH_COMPANY_NOT_FOUND));

        wishCompanyRepository.delete(wishCompany);
    }

    @Retryable(
        retryFor = {RetryableSaraminException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void updateCompanyRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchAllRecruitment(keyword);
        log.info("전체 공고 개수 : {}", apiResp.jobs().job().size());

        recruitmentSyncService.persistNewRecruitmentsAndNotify(apiResp, false);
    }

    @Retryable(
        retryFor = {RetryableSaraminException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void updateCompanyOpenRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchOpenRecruitment(keyword);
        log.info("공채 공고 개수 : {}", apiResp.jobs().job().size());

        recruitmentSyncService.persistNewRecruitmentsAndNotify(apiResp, true);
    }

    @Recover
    public void recruitingRecover(RetryableSaraminException ex, String keyword) {
        log.error("[Saramin API No React] {}에 대한 미응답", keyword);
        Sentry.captureException(ex);
    }

    @Override
    @Retryable(
        retryFor = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    public void cleanExpiredRecruitments(LocalDateTime now) {
        expireRecruitments(now);
        closeRecruitingStatusForInactiveCompanies();
    }

    @Recover
    public void recoverCleanExpiredRecruitments(
        TransientDataAccessException ex, LocalDateTime now
    ) {
        log.error("[RecruitCleanup] 마감 공고 삭제 실패 - 시각: {}, 메시지: {}", now, ex.getMessage(), ex);
        Sentry.captureException(ex);
    }

    private void expireRecruitments(LocalDateTime now) {
        List<Recruitment> expiredBefore = recruitmentRepository.findExpiredBefore(now);
        expiredBefore.forEach(r -> r.expired(now));
    }

    private void closeRecruitingStatusForInactiveCompanies() {
        List<CompanyActiveCount> activeList = recruitmentRepository.countActiveByCompany();

        List<Long> companyIds = activeList.stream()
            .map(CompanyActiveCount::companyId)
            .toList();

        Map<Long, Company> companyMap = companyQueryService.findByIds(companyIds).stream()
            .collect(Collectors.toMap(Company::getId, Function.identity()));

        for (CompanyActiveCount c : activeList) {
            Company company = companyMap.get(c.companyId());
            company.closeRecruitingIfNoActivePostings(c.recruitmentCount());
        }
    }
}
