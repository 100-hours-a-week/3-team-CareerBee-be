package org.choon.careerbee.config.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Long DEFAULT_TTL = 30L;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. 공통 직렬화(Serialization) 설정
        RedisCacheConfiguration commonConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()));

        // 2. 캐시 이름별로 TTL을 다르게 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("companyStaticDetail", commonConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("recruitments", commonConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("recentIssue", commonConfig.entryTtl(Duration.ofDays(7)));
        cacheConfigurations.put("wishCount", commonConfig.entryTtl(Duration.ofMinutes(10)));

        // 3. CacheManager 빌더를 사용하여 최종 CacheManager 객체 생성
        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(commonConfig.entryTtl(Duration.ofMinutes(DEFAULT_TTL)))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
