package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class ProjectRule implements ProgressRule {

    @Override
    public int apply(Member member) {
        int count = member.getProjectCount();

        if (count == 0) {
            return 0;
        }
        if (count <= 2) {
            return 70;
        }
        if (count <= 5) {
            return 120;
        }

        return 150;
    }
}
