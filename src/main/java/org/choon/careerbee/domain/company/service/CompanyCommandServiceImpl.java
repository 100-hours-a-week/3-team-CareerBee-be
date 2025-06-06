package org.choon.careerbee.domain.company.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp.Job;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.enums.RecruitingStatus;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.repository.recruitment.RecruitmentRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {

    private static final int RECRUITING_STATUS_CLOSED = 0;
    private static final DateTimeFormatter SARAMIN_DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final CompanyApiClient companyApiClient;

    private final RecruitmentRepository recruitmentRepository;
    private final WishCompanyRepository wishCompanyRepository;
    private final MemberQueryService memberQueryService;
    private final CompanyQueryService companyQueryService;

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

    @Override
    public void updateCompanyRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchAllRecruitment(keyword);
        log.info("1️⃣ 전체 공고 개수 : {}", apiResp.jobs().job().size());

        persistNewRecruitmentsAndNotify(apiResp, false);
    }

    @Override
    public void updateCompanyOpenRecruiting(String keyword) {
        SaraminRecruitingResp apiResp = companyApiClient.searchOpenRecruitment(keyword);
        log.info("2️⃣ 공채 공고 개수 : {}", apiResp.jobs().job().size());

        persistNewRecruitmentsAndNotify(apiResp, true);
    }

    private void persistNewRecruitmentsAndNotify(
        SaraminRecruitingResp apiResp,
        boolean isOpenRecruitment
    ) {
        List<Job> jobs = apiResp.jobs().job();

        // 1. ID, 회사명 추출
        List<String> companyNames = jobs.stream()
            .map(j -> j.company().detail().name())
            .distinct()
            .collect(Collectors.toList());

        List<Long> jobIds = jobs.stream()
            .map(SaraminRecruitingResp.Job::id)
            .collect(Collectors.toList());

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
            recruitmentRepository.saveAll(toSave);
        }

        // 5. 알림 이벤트 발행
        if (isOpenRecruitment) {

        }
    }

    private LocalDateTime parseSaraminDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(dateStr, SARAMIN_DT_FMT)
            .toLocalDateTime();
    }

}
