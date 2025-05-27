package org.choon.careerbee.fixture;

import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.member.entity.Member;

public class MemberFixture {

    public static Member createMember(String nickname, String email, Long providerId) {
        return Member.builder()
            .nickname(nickname)
            .email(email)
            .oAuthProvider(OAuthProvider.KAKAO)
            .providerId(providerId)
            .build();
    }
}
