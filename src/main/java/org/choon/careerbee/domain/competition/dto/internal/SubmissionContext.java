package org.choon.careerbee.domain.competition.dto.internal;

import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.member.entity.Member;

public record SubmissionContext(
    Competition competition,
    Member member
) {

}
