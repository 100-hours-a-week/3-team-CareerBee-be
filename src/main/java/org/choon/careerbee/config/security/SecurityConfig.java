package org.choon.careerbee.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.auth.repository.TokenRepository;
import org.choon.careerbee.filter.jwt.JwtAuthenticationFilter;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        return request -> {
//            CorsConfiguration config = new CorsConfiguration();
//            config.setAllowedHeaders(Collections.singletonList("*"));
//            config.setAllowedMethods(Collections.singletonList("*"));
//            config.setAllowedOriginPatterns(Arrays.asList(
//                "http://localhost:5173",
//                "http://localhost:5500",
//                "http://127.0.0.1:5173",
//                "http://127.0.0.1:5500",
//                "https://www.careerbee.co.kr",
//                "https://ai.careerbee.co.kr",
//                "https://dev.careerbee.co.kr",
//                "https://dev-ai.careerbee.co.kr",
//                "https://www.junjo.o-r.kr"
//            ));
//            config.setAllowCredentials(true);
//            return config;
//        };
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5500",
            "https://www.careerbee.co.kr",
            "https://ai.careerbee.co.kr",
            "https://dev.careerbee.co.kr",
            "https://dev-ai.careerbee.co.kr",
            "https://www.junjo.o-r.kr"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 🔥 이거 꼭 필요
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 토큰 방식을 위한 STATELESS 선언
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 권한 규칙 설정 (API 명세에 맞게 수정 필요)
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers(
                    "/health-check",
                    "/api/v1/companies",
                    "/api/v1/auth/oauth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/competitions/rankings",
                    "/competitions/ids",
                    "/actuator/**"
                )
                .permitAll()  // 인증 없이 접근 가능한 URI 추가
                .requestMatchers(
                    "/users/**",
                    "/api/v1/members/competitions/rankings"
                ).hasRole("MEMBER")
                .anyRequest().permitAll()  // 그 외 요청은 인가처리를 할 필요가 없음
            )
            // CORS 해결하기 위한 코드 추가
            .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
            // 커스텀 JWT 핸들러 및 엔트리 포인트를 사용하기 위해 httpBasic disable
            .httpBasic(AbstractHttpConfigurer::disable)
            // JWT Filter 를 필터체인에 끼워넣어줌
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, tokenRepository, objectMapper),
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
