package org.choon.careerbee.config.retry;

import org.springframework.batch.core.repository.persistence.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class BatchRetryConfig {

    @Bean
    public RetryListener stepRetryRecorder() {
        return new RetryListener() {

            @Override
            public <T, E extends Throwable> boolean open(RetryContext ctx,
                RetryCallback<T, E> cb) {
                // 초기화 필요 시 (true 반환해야 재시도 진행)
                return true;
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext ctx,
                RetryCallback<T, E> cb,
                Throwable ex) {
                StepExecution se = (StepExecution) ctx.getAttribute("stepExecution");
                if (se != null) {
                    se.getExecutionContext().("retryCount", ctx.getRetryCount());
                    se.getExecutionContext().putString("lastRetryEx", ex.getClass().getName());
                }
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext ctx,
                RetryCallback<T, E> cb,
                Throwable ex) {
                // 마지막에 정리 필요 시 사용 (ex != null 이면 최종 실패)
            }
        };
    }

    @Bean("dbRetryTemplate")
    public RetryTemplate dbRetryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(3000, 2.0, 15000)
            .retryOn(TransientDataAccessException.class)
            .build();
    }


}
