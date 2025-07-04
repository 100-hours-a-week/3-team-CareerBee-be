package org.choon.careerbee.domain.company.service;

import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;

public interface RecruitmentSyncService {

    void persistNewRecruitmentsAndNotify(SaraminRecruitingResp apiResp, boolean isOpenRecruitment);
}
