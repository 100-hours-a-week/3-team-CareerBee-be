package org.choon.careerbee.domain.company.repository.wish.custom;

import org.choon.careerbee.domain.company.dto.response.WishCompanyIdResp;
import org.choon.careerbee.domain.member.entity.Member;

public interface WishCompanyCustomRepository {

    WishCompanyIdResp fetchWishCompanyIdsByMember(Member member);
}
