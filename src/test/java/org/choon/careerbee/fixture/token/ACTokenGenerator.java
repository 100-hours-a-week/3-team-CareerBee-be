package org.choon.careerbee.fixture.token;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Disabled("테스트 전용 토큰 생성시 사용")
@ActiveProfiles("test")   // test 프로파일에 맞게 설정
class ACTokenGenerator {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 부하 테스트용 JWT(Access Token) CSV 파일 생성 경로: build/tmp/tokens_<timestamp>.csv
     */
    @Test
    @DisplayName("부하 테스트용 JWT CSV 파일 생성")
    void generateLoadTestTokensCsv() throws Exception {

        String fileName = "tokens_" + LocalDateTime.now()
            .toString().replace(":", "-") + ".csv";
        Path out = Path.of("build", "tmp", fileName);
        Files.createDirectories(out.getParent());

        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            for (long memberId = 100; memberId < 200; memberId++) {
                String token = jwtUtil.createToken(memberId, TokenType.ACCESS_TOKEN);

                // (2) 토큰 null 체크 -- 문제 진단에 도움
                if (token == null) {
                    throw new IllegalStateException("token null for id " + memberId);
                }

                bw.write("Bearer " + token);
                bw.newLine();
            }
        }
        System.out.println("✅ JWT CSV 생성 완료: " + out.toAbsolutePath());
    }

    @Test
    @DisplayName("AI 서버와 통신 부하 테스트용 JWT CSV 파일 생성")
    void generateAiLoadTestTokensCsv() throws Exception {

        String fileName = "ai_tokens_" + LocalDateTime.now()
            .toString().replace(":", "-") + ".csv";
        Path out = Path.of("build", "tmp", fileName);
        Files.createDirectories(out.getParent());

        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            for (long memberId = 21; memberId < 31; memberId++) {
                String token = jwtUtil.createToken(memberId, TokenType.ACCESS_TOKEN);

                // (2) 토큰 null 체크 -- 문제 진단에 도움
                if (token == null) {
                    throw new IllegalStateException("token null for id " + memberId);
                }

                bw.write("Bearer " + token);
                bw.newLine();
            }
        }
        System.out.println("✅ JWT CSV 생성 완료: " + out.toAbsolutePath());
    }
}
