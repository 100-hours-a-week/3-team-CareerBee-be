package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class WorkRule implements ProgressRule {

    @Override
    public int apply(Member member) {
        if (member.getWorkPeriod() >= 1) {
            return 40;
        }

        return 0;
    }
}
