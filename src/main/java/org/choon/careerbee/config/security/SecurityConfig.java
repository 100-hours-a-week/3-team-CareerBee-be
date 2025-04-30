package org.choon.careerbee.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//  @Bean
//  CorsConfigurationSource corsConfigurationSource() {
//    return request -> {
//      CorsConfiguration config = new CorsConfiguration();
//      config.setAllowedHeaders(Collections.singletonList("*"));
//      config.setAllowedMethods(Collections.singletonList("*"));
//      config.setAllowedOriginPatterns(Arrays.asList(
//          "http://localhost:3000",
//          "http://localhost:5500",
//          "http://127.0.0.1:5500",
//          "http://127.0.0.1:3000"
//      ));
//      config.setAllowCredentials(true);
//      return config;
//    };
//  }

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
                "/auth/v1/auth/oauth"
            )
            .permitAll()  // 인증 없이 접근 가능한 URI 추가
            .requestMatchers("/users/**").hasRole("MEMBER")
            .anyRequest().permitAll()  // 그 외 요청은 인가처리를 할 필요가 없음
        )
        // CORS 해결하기 위한 코드 추가
//        .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
        // 커스텀 JWT 핸들러 및 엔트리 포인트를 사용하기 위해 httpBasic disable
        .httpBasic(AbstractHttpConfigurer::disable)
        // JWT Filter 를 필터체인에 끼워넣어줌
//        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, redisUtil),
//            UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
