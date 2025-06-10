package org.choon.careerbee.domain.member.progress;

import org.choon.careerbee.domain.member.entity.Member;

public interface ResumeProgressPolicy {

    int calculate(Member member);
}
