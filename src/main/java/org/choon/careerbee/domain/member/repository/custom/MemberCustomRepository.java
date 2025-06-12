package org.choon.careerbee.domain.member.repository.custom;

import java.util.List;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;

public interface MemberCustomRepository {

    MyInfoResp fetchMyInfoByMemberId(Long memberId);

    String getNicknameByMemberId(Long memberId);

    List<Long> findAllMemberIds();
}
