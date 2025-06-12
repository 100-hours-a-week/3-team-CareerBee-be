package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class AdditionalExpRule implements ProgressRule {

    @Override
    public int apply(Member member) {
        if (member.getAdditionalExperiences() != null) {
            return 10;
        }

        return 0;
    }
}
