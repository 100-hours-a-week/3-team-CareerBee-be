package org.choon.careerbee.domain.company.repository.wish.custom;

import java.util.List;
import java.util.Map;
import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.member.dto.response.WishCompaniesResp;

public interface WishCompanyCustomRepository {

    WishCompanyIdResp fetchWishCompanyIdsByMember(Long memberId);

    WishCompaniesResp fetchWishCompaniesByMemberId(Long memberId, Long cursor, int size);

    List<Long> getMemberIdsByCompanyId(Long companyId);

    Map<Long, List<Long>> getWishMemberIdsGroupedByCompanyId(List<Long> list);

    Long fetchWishCountById(Long companyId);
}
