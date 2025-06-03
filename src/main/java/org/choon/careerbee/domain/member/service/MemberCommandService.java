package org.choon.careerbee.domain.member.service;

import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.entity.Member;

public interface MemberCommandService {

    Member forceJoin(OAuthInfoResponse oAuthInfoResponse);

    void updateResumeInfo(UpdateResumeReq updateResumeReq, Long accessMemberId);
}
