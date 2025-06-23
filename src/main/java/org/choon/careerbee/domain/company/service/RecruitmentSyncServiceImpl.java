package org.choon.careerbee.domain.company.service;

import io.sentry.Sentry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp.Job;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
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

    @Retryable(
        retryFor = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 3000, multiplier = 2))
    @Override
    public void persistNewRecruitmentsAndNotify(
        SaraminRecruitingResp apiResp,
        boolean isOpenRecruitment
    ) {
        List<Job> jobs = apiResp.jobs().job();

        // 1. ID, 회사명 추출
        List<String> companyNames = jobs.stream()
            .map(j -> j.company().detail().name())
            .distinct()
            .toList();

        List<Long> jobIds = jobs.stream()
            .map(SaraminRecruitingResp.Job::id)
            .toList();

        // 2. DB에서 한 번에 조회
        List<Company> companies = companyQueryService.findBySaraminNameIn(companyNames);
        Map<String, Company> companyMap = companies.stream()
            .collect(Collectors.toMap(Company::getSaraminName, Function.identity()));

        // 이미 등록된 공고 ID만 꺼내오기
        Set<Long> existingIds = recruitmentRepository
            .findRecruitingIdByRecruitingIdIn(jobIds)
            .stream().collect(Collectors.toSet());

        // 3. 메모리 필터링 & 엔티티 생성
        List<Recruitment> toSave = new ArrayList<>();
        Map<String, Set<Long>> toNoti = new HashMap<>();
        for (SaraminRecruitingResp.Job job : jobs) {
            if (job.active() == RECRUITING_STATUS_CLOSED) {
                continue;
            }

            Company company = companyMap.get(job.company().detail().name());
            if (company == null || existingIds.contains(job.id())) {
                continue;
            }

            // 상태 변경
            company.changeRecruitingStatus(RecruitingStatus.ONGOING);

            // 해당 기업을 관심목록으로 등록한 사람들을 넣기
            if (isOpenRecruitment) {
                List<Long> wishMemberIds = wishCompanyRepository.getMemberIdsByCompanyId(
                    company.getId());

                toNoti
                    .computeIfAbsent(company.getName(), k -> new HashSet<>())
                    .addAll(wishMemberIds);
            }

            toSave.add(Recruitment.from(
                company,
                job.id(),
                job.url(),
                job.position().title(),
                parseSaraminDate(job.postingDate()),
                parseSaraminDate(job.expirationDate())
            ));
        }

        if (!toSave.isEmpty()) {
            // 4. Batch Insert
            recruitmentRepository.batchInsert(toSave);
        }

        // 5. 알림 이벤트 발행
        if (isOpenRecruitment && !toNoti.isEmpty()) {
            eventPublisher.publishEvent(
                new OpenRecruitingEvent(toNoti)
            );
        }
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

    private LocalDateTime parseSaraminDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(dateStr, SARAMIN_DT_FMT)
            .toLocalDateTime();
    }
}
