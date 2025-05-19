package org.choon.careerbee.domain.company.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.api.CompanyApiClient;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
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
@Transactional
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {

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
    public void updateCompanyRecruiting() {
        // 1. 사람인 API 호출
        SaraminRecruitingResp apiResp = companyApiClient.searchJobs();

        // 2. 공고 반복 처리
        for (SaraminRecruitingResp.Job job : apiResp.jobs().job()) {

            /* --- 2‑1. Company 존재 여부(name 기준) --- */
            Optional<Company> optCompany =
                companyQueryService.findBySaraminName(job.company().detail().name());

            if (optCompany.isEmpty()) {
                continue;    // 회사가 없으면 아무 작업도 하지 않음
            }

            Company company = optCompany.get();

            /* --- 2‑2. Recruitment 중복 여부(recruitingId) --- */
            boolean alreadyExists =
                recruitmentRepository.existsByRecruitingId(job.id());

            if (alreadyExists) {
                continue;    // 중복 공고는 스킵
            }

            /* --- 2‑3. Company 상태 변경 (Dirty Checking) --- */
            company.changeRecruitingStatus(RecruitingStatus.ONGOING);

            /* --- 2‑4. Recruitment 엔티티 저장 --- */
            recruitmentRepository.save(Recruitment.from(
                company,
                job.id(),
                job.url(),
                job.position().title(),
                parseSaraminDate(job.postingDate()),
                parseSaraminDate(job.expirationDate())
            ));
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
