package org.choon.careerbee.domain.company.service;

import java.time.LocalDateTime;

public interface CompanyCommandService {

    void registWishCompany(Long accessMemberId, Long companyId);

    void deleteWishCompany(Long accessMemberId, Long companyId);

    void updateCompanyRecruiting(String keyword);

    void updateCompanyOpenRecruiting(String keyword);

    void cleanExpiredRecruitments(LocalDateTime now);
}
