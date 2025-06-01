package org.choon.careerbee.domain.member.service;

import java.util.Optional;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface MemberQueryService {

    boolean isMemberExistByEmail(String email);

    Optional<Long> getMemberIdByEmail(String email);

    MyInfoResp getMyInfoByMemberId(Long memberId);

    Member findById(Long memberId);
}
