package org.choon.careerbee.domain.company.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void updateCompanyRecruiting() {
        SaraminRecruitingResp apiResp = companyApiClient.searchAllRecruitment();
        log.info("1️⃣ 전체 공고 개수 : {}", apiResp.jobs().job().size());

        persistNewRecruitmentsAndNotify(apiResp, false);
    }

    @Override
    public void updateCompanyOpenRecruiting() {
        SaraminRecruitingResp apiResp = companyApiClient.searchOpenRecruitment();
        log.info("2️⃣ 공채 공고 개수 : {}", apiResp.jobs().job().size());

        persistNewRecruitmentsAndNotify(apiResp, true);
    }

    private void persistNewRecruitmentsAndNotify(SaraminRecruitingResp apiResp,
        boolean isOpenRecruitment) {
        for (SaraminRecruitingResp.Job job : apiResp.jobs().job()) {
            if (job.active() == RECRUITING_STATUS_CLOSED) {
                continue; // 마감된 공고라면 continue
            }

            Optional<Company> optCompany =
                companyQueryService.findBySaraminName(job.company().detail().name());

            if (optCompany.isEmpty() || recruitmentRepository.existsByRecruitingId(job.id())) {
                continue; // 매칭 안된 회사 스킵
            }

            Company company = optCompany.get();
            company.changeRecruitingStatus(RecruitingStatus.ONGOING);

            recruitmentRepository.save(Recruitment.from(
                company,
                job.id(),
                job.url(),
                job.position().title(),
                parseSaraminDate(job.postingDate()),
                parseSaraminDate(job.expirationDate())
            ));

            if (isOpenRecruitment) {
                // Todo : 공채의 경우 알림 발송 로직 구현
                // 1. 알림 생성
                // 2. 해당 기업을 관심기업으로 등록한 유저들 조회
                // 3. 생성한 알림을 유저들에게 발송
            }
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
