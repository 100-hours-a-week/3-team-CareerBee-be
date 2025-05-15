package org.choon.careerbee.domain.member.repository.custom;

import org.choon.careerbee.domain.member.dto.response.MyInfoResp;

public interface MemberCustomRepository {

    MyInfoResp fetchMyInfoByMemberId(Long memberId);
}
