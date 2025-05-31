package org.choon.careerbee.config.web;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.interceptor.logging.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API 엔드포인트 전역 측정
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/**")
            .order(1);
    }

}
