package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.springframework.stereotype.Component;

@Component
public class MajorRule implements ProgressRule {

    @Override
    public int apply(Member member) {
        MajorType majorType = member.getMajorType();

        if (majorType == MajorType.MAJOR) {
            return 70;
        }

        return 0;
    }
}
