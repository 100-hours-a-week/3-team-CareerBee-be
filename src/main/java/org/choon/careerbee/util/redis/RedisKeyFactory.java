package org.choon.careerbee.util.redis;

import org.choon.careerbee.domain.interview.domain.enums.ProblemType;

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

    // 필요 시 추가 예시:
    public static String rankCacheKey(ProblemType type, String date) {
        return "rank:%s:%s".formatted(type.getPrefix(), date);
    }

    public static String dailySolvedKey(Long memberId, ProblemType type) {
        return "solved:%d:%s:daily".formatted(memberId, type.getPrefix());
    }
}
