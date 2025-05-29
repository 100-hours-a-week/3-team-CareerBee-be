package org.choon.careerbee.fixture;

import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.member.entity.Member;

public class WishCompanyFixture {

    public static WishCompany createWishCompany(
        Company company,
        Member member
    ) {
        return WishCompany.of(member, company);
    }
}
