package org.choon.careerbee.util.date;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static long getSecondsUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(KOREA_ZONE);
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(KOREA_ZONE);
        return Duration.between(now, midnight).getSeconds();
    }
}
