package org.choon.careerbee.util.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.choon.careerbee.domain.interview.domain.enums.ProblemType;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    public static String canSolveKey(Long memberId, ProblemType type) {
        return key(memberId, type, "canSolve");
    }

    public static String freeCountKey(Long memberId, ProblemType type) {
        return key(memberId, type, "freeCount");
    }

    public static String payCountKey(Long memberId, ProblemType type) {
        return key(memberId, type, "payCount");
    }

    private static String key(Long memberId, ProblemType type, String suffix) {
        return "member:%d:%s:%s".formatted(memberId, type.getPrefix(), suffix);
    }

    public static String todayKey(LocalDate today) {
        return String.format(
            "%s",
            today.format(DateTimeFormatter.BASIC_ISO_DATE)
        );
    }

    public static String compParticipationKey(Long compId, Long memberId) {
        return String.format(
            "comp:part:%d:member:%d",
            compId, memberId
        );
    }

    public static String competitionRankKey(LocalDate today) {
        return String.format(
            "competition:rank:%s",
            todayKey(today)
        );
    }

    public static String competitionIdKey(LocalDate today) {
        return String.format(
            "competition:id:%s",
            todayKey(today)
        );
    }

    public static String memberRankingKey(Long memberId, LocalDate today) {
        return String.format(
            "competition:ranking:member:%d:%s",
            memberId, todayKey(today)
        );
    }
}
