package org.choon.careerbee.domain.member.progress;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.progress.rules.ProgressRule;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultResumeProgressPolicy implements ResumeProgressPolicy {

    private final List<ProgressRule> rules;

    @Override
    public int calculate(Member member) {
        return rules.stream()
            .mapToInt(r -> r.apply(member))
            .sum();
    }
}
