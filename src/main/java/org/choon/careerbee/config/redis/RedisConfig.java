package org.choon.careerbee.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    private static final String REDISSON_HOST_PREFIX = "redis://";
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public RedisConfig(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
//        config.setCodec(new StringCodec());
        config.setCodec(new TypedJsonJacksonCodec(Object.class, objectMapper));
        SingleServerConfig singleServerConfig = config.useSingleServer()
            .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);

        if (isProdProfile()) {
            singleServerConfig.setUsername(redisUsername).setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

    @Bean
    public RedissonConnectionFactory redisConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    public boolean isProdProfile() {
        return environment.acceptsProfiles("prod");
    }
}
