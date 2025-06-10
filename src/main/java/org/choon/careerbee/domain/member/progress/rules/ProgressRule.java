package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;

public interface ProgressRule {

    int apply(Member member);
}
