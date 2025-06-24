package org.choon.careerbee.domain.member.repository.custom;

import java.util.List;
import org.choon.careerbee.domain.auth.dto.internal.MemberAuthInfo;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;

public interface MemberCustomRepository {

    MemberAuthInfo getMemberAuthInfo(Long memberId);

    MyInfoResp fetchMyInfoByMemberId(Long memberId);

    String getNicknameByMemberId(Long memberId);

    List<Long> findAllMemberIds();
}
