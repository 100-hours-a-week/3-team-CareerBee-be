package org.choon.careerbee.domain.member.service;

import java.time.LocalDateTime;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.member.dto.request.AdvancedResumeUpdateReq;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeResp;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface MemberCommandService {

    Member forceJoin(OAuthInfoResponse oAuthInfoResponse);

    void updateResumeInfo(UpdateResumeReq updateResumeReq, Long accessMemberId);

    void updateProfileInfo(UpdateProfileInfoReq updateProfileInfoReq, Long accessMemberId);

    void withdrawal(WithdrawalReq withdrawalReq, Long accessMemberId, LocalDateTime withdrawAt);

    ResumeDraftResp generateResumeDraft(Long accessMemberId);

    ExtractResumeResp extractResumeInfoFromAi(UploadCompleteReq uploadCompleteReq);

    AdvancedResumeInitResp generateAdvancedResumeInit(Long accessMemberId);

    AdvancedResumeResp generateAdvancedResumeUpdate(
        AdvancedResumeUpdateReq advancedResumeUpdateReq, Long accessMemberId
    );

    void extractResumeInfoFromAiAsync(
        UploadCompleteReq uploadCompleteReq, Long accessMemberId
    );

    void generateAdvancedResumeInitAsync(Long accessMemberId);

    void generateAdvancedResumeUpdateAsync(
        AdvancedResumeUpdateReq advancedResumeUpdateReq,
        Long accessMemberId
    );
}
