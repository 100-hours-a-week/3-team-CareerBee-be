package org.choon.careerbee.domain.member.progress.rules;

import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class CertificationRule implements ProgressRule {

    @Override
    public int apply(Member member) {
        int count = member.getCertificationCount();

        if (count == 0) {
            return 0;
        }
        if (count <= 2) {
            return 10;
        }
        if (count <= 5) {
            return 20;
        }

        return 25;
    }
}
