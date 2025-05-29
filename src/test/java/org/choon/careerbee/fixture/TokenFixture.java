package org.choon.careerbee.fixture;

import org.choon.careerbee.domain.auth.entity.Token;
import org.choon.careerbee.domain.auth.entity.enums.TokenStatus;
import org.choon.careerbee.domain.member.entity.Member;

public class TokenFixture {

    public static Token createToken(Member member, String tokenValue, TokenStatus tokenStatus) {
        return Token.builder()
            .member(member)
            .status(tokenStatus)
            .tokenValue(tokenValue)
            .build();
    }

}
