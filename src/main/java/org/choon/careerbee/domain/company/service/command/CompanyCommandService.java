package org.choon.careerbee.domain.company.service.command;

import java.time.LocalDateTime;
import java.util.List;
import org.choon.careerbee.domain.company.dto.request.RecentIssueUpdateReq;

public interface CompanyCommandService {

    void registWishCompany(Long accessMemberId, Long companyId);

    void deleteWishCompany(Long accessMemberId, Long companyId);

    void updateCompanyRecruiting(String keyword);

    void updateCompanyOpenRecruiting(String keyword);

    void cleanExpiredRecruitments(LocalDateTime now);

    void updateRecentIssue(List<RecentIssueUpdateReq> updateRequests);
}
