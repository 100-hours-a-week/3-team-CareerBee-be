package org.choon.careerbee.domain.member.progress.rules;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TierRule implements ProgressRule {

    /**
     * tier 코드별(level1~5) 점수 테이블
     */
    private static final Map<String, int[]> TIER_TABLE = Map.of(
        "BR", new int[]{20, 16, 12, 8, 4},
        "SL", new int[]{80, 66, 52, 38, 24},
        "GL", new int[]{130, 120, 110, 100, 90},
        "PL", new int[]{164, 158, 152, 146, 140},
        "DI", new int[]{186, 182, 178, 174, 170},
        "RU", new int[]{198, 196, 194, 192, 190}
    );

    private static final int MASTER_SCORE = 200;

    @Override
    public int apply(Member member) {
        String tier = member.getPsTier();
        if (tier == null || tier.isBlank()) {
            return 0;
        }

        tier = tier.trim().toUpperCase();

        if (tier.equals("MS")) {
            return MASTER_SCORE;
        }

        String code = tier.substring(0, 2);
        int level = Integer.parseInt(tier.substring(2)); // “BR3” → 3

        int[] scores = TIER_TABLE.get(code);
        return scores[level - 1];
    }
}
