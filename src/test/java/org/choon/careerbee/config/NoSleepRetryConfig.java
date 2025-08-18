package org.choon.careerbee.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.Sleeper;

@TestConfiguration
public class NoSleepRetryConfig {
    @Bean
    Sleeper sleeper() {
        return millis -> {};
    }
}
