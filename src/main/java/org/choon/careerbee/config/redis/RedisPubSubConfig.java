package org.choon.careerbee.config.redis;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.pubsub.RedisSubscriber;
import org.choon.careerbee.common.pubsub.enums.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final RedisSubscriber redisSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        List<String> topics = List.of(
            Channel.RESUME_EXTRACTED.getValue(),
            Channel.ADVANCED_RESUME_INIT.getValue(),
            Channel.ADVANCED_RESUME_UPDATE.getValue(),
            Channel.PROBLEM_FEEDBACK.getValue(),
            Channel.AI_ERROR_CHANNEL.getValue()
        );

        for (String topic : topics) {
            container.addMessageListener(listenerAdapter, new PatternTopic(topic));
        }

        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter() {

        return new MessageListenerAdapter(redisSubscriber, "onMessage");
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
