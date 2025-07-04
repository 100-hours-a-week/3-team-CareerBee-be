package org.choon.careerbee.domain.company.service;

import io.sentry.Sentry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.internal.JobContext;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp.Job;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.query.CompanyQueryService;
import org.choon.careerbee.domain.notification.dto.event.OpenRecruitingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecruitmentSyncServiceImpl implements RecruitmentSyncService {

    private static final int RECRUITING_STATUS_CLOSED = 0;
    private static final DateTimeFormatter SARAMIN_DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final CompanyQueryService companyQueryService;
    private final RecruitmentRepository recruitmentRepository;
    private final WishCompanyRepository wishCompanyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Retryable(
        retryFor = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    public void persistNewRecruitmentsAndNotify(
        SaraminRecruitingResp apiResp,
        boolean isOpenRecruitment
    ) {
        List<Job> jobs = apiResp.jobs().job();
        JobContext context = extractJobContext(jobs);

        List<Recruitment> toSave = new ArrayList<>();
        Map<String, Set<Long>> toNoti = new HashMap<>();

        for (Job job : jobs) {
            processJob(job, context, isOpenRecruitment, toSave, toNoti);
        }

        saveNewRecruitments(toSave);
        notifyWishMembersIfNeeded(isOpenRecruitment, toNoti);
    }

    @Recover
    public void recruitmentSaveRecover(
        TransientDataAccessException ex,
        SaraminRecruitingResp apiResp,
        boolean isOpenRecruitment
    ) {
        log.error("[공고 저장 실패] 사람인 응답 건수={}, openRecruit={}", apiResp.jobs().job().size(),
            isOpenRecruitment, ex);
        Sentry.captureException(ex);
    }

    private JobContext extractJobContext(List<Job> jobs) {
        List<String> companyNames = jobs.stream()
            .map(j -> j.company().detail().name())
            .distinct()
            .toList();

        List<Long> jobIds = jobs.stream()
            .map(Job::id)
            .toList();

        List<Company> companies = companyQueryService.findBySaraminNameIn(companyNames);
        Set<Long> existingIds = recruitmentRepository.findRecruitingIdByRecruitingIdIn(jobIds)
            .stream().collect(Collectors.toSet());

        Map<String, Company> companyMap = companies.stream()
            .collect(Collectors.toMap(Company::getSaraminName, Function.identity()));

        Map<Long, List<Long>> wishMemberMap = wishCompanyRepository
            .getWishMemberIdsGroupedByCompanyId(
                companies.stream().map(Company::getId).toList()
            );

        return new JobContext(companyMap, existingIds, wishMemberMap);
    }

    private void processJob(
        Job job,
        JobContext context,
        boolean isOpenRecruitment,
        List<Recruitment> toSave,
        Map<String, Set<Long>> toNoti
    ) {
        if (job.active() == RECRUITING_STATUS_CLOSED) {
            return;
        }

        Company company = context.companyMap().get(job.company().detail().name());
        if (company == null || context.existingIds().contains(job.id())) {
            return;
        }

        company.changeRecruitingStatus(RecruitingStatus.ONGOING);

        if (isOpenRecruitment) {
            List<Long> wishMemberIds = context.wishMemberMap()
                .getOrDefault(company.getId(), Collections.emptyList());

            toNoti.computeIfAbsent(company.getName(), k -> new HashSet<>()).addAll(wishMemberIds);
        }

        toSave.add(Recruitment.from(
            company,
            job.id(),
            job.url(),
            job.position().title(),
            parseSaraminDate(job.postingDate()).orElse(null),
            parseSaraminDate(job.expirationDate()).orElse(null)
        ));
    }

    private void saveNewRecruitments(List<Recruitment> toSave) {
        if (!toSave.isEmpty()) {
            recruitmentRepository.batchInsert(toSave);
        }
    }

    private void notifyWishMembersIfNeeded(boolean isOpenRecruitment,
        Map<String, Set<Long>> toNoti) {
        if (isOpenRecruitment && !toNoti.isEmpty()) {
            eventPublisher.publishEvent(new OpenRecruitingEvent(toNoti));
        }
    }

    private Optional<LocalDateTime> parseSaraminDate(String dateStr) {
        return Optional.ofNullable(dateStr)
            .filter(date -> !date.isBlank())
            .map(date -> OffsetDateTime.parse(date, SARAMIN_DT_FMT).toLocalDateTime());
    }
}
