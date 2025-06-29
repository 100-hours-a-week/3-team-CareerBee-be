package org.choon.careerbee.domain.company.repository.wish.custom;

import java.util.List;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface WishCompanyCustomRepository {

    WishCompanyIdResp fetchWishCompanyIdsByMember(Member member);

    WishCompaniesResp fetchWishCompaniesByMemberId(Long memberId, Long cursor, int size);

    List<Long> getMemberIdsByCompanyId(Long companyId);

    Long fetchWishCountById(Long companyId);
}
