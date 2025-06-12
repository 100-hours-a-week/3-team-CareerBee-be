package org.choon.careerbee.domain.company.repository.wish.custom;

import java.util.List;
import java.util.Optional;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.company.dto.response.WishCompanyProgressResp;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface WishCompanyCustomRepository {

    WishCompanyIdResp fetchWishCompanyIdsByMember(Member member);

    WishCompaniesResp fetchWishCompaniesByMemberId(Long memberId, Long cursor, int size);

    Optional<WishCompanyProgressResp> fetchWishCompanyAndMemberProgress(
        Long wishCompanyId, Long accessMemberId
    );

    List<Long> getMemberIdsByCompanyId(Long companyId);
}
