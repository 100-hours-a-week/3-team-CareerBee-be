package org.choon.careerbee.config.retry;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry(proxyTargetClass = true)
@Configuration
public class RetryConfig {

}
