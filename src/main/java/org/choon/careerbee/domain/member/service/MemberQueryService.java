package org.choon.careerbee.domain.member.service;

import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface MemberQueryService {

    MyInfoResp getMyInfoByMemberId(Long memberId);

    Member findById(Long memberId);

    Member getReferenceById(Long memberId);
}
