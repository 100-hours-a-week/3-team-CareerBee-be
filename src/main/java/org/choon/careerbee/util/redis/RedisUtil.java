package org.choon.careerbee.util.redis;

import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtil {

    public static int getOrInitCount(
        RedissonClient redissonClient, String key, long ttlSeconds
    ) {
        RBucket<Integer> bucket = redissonClient.getBucket(key);
        Integer count = bucket.get();
        if (count == null) {
            bucket.set(0, ttlSeconds, TimeUnit.SECONDS);
            return 0;
        }
        return count;
    }

    public static void setCount(
        RedissonClient redissonClient, String key, int value, long ttlSeconds
    ) {
        redissonClient.getBucket(key).set(value, ttlSeconds, TimeUnit.SECONDS);
    }

    public static void setBoolean(
        RedissonClient redissonClient, String key, boolean value, long ttlSeconds
    ) {
        redissonClient.getBucket(key).set(value, ttlSeconds, TimeUnit.SECONDS);
    }

    public static boolean getOrDefaultBoolean(
        RedissonClient redissonClient, String key, boolean defaultValue, long ttlSeconds
    ) {
        RBucket<Boolean> bucket = redissonClient.getBucket(key);
        Boolean value = bucket.get();
        if (value == null) {
            bucket.set(defaultValue, ttlSeconds, TimeUnit.SECONDS);
            return defaultValue;
        }
        return value;
    }
}
