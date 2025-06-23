package org.choon.careerbee.domain.competition.dto.response;

import org.choon.careerbee.domain.member.entity.Member;

public record Top10Info(
    Long ranking,
    Member member
) {

}
