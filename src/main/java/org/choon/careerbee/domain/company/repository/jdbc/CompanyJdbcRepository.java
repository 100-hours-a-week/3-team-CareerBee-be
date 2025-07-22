package org.choon.careerbee.domain.company.repository.jdbc;

import java.util.List;
import org.choon.careerbee.domain.company.dto.request.RecentIssueUpdateReq;

public interface CompanyJdbcRepository {

    void batchUpdateRecentIssues(List<RecentIssueUpdateReq> updateRequests);
}
