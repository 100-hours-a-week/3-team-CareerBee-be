package org.choon.careerbee.domain.member.service;

import java.time.LocalDateTime;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.entity.Member;

public interface MemberCommandService {

    Member forceJoin(OAuthInfoResponse oAuthInfoResponse);

    void updateResumeInfo(UpdateResumeReq updateResumeReq, Long accessMemberId);

    void withdrawal(WithdrawalReq withdrawalReq, Long accessMemberId, LocalDateTime withdrawAt);
}
