package org.choon.careerbee.domain.auth.dto.internal;

import org.choon.careerbee.domain.member.entity.enums.RoleType;

public record MemberAuthInfo(
    Long memberId,
    RoleType role
) {

}
