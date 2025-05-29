package org.choon.careerbee.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Getter
@Slf4j
public class JwtUtil {

    private static final String ID = "id";
    private static final String BEARER = "Bearer ";

    private final String SECRET_KEY;
    private final long ACCESS_TOKEN_EXPIRATION_TIME;
    private final long REFRESH_TOKEN_EXPIRATION_TIME;
    private final UserDetailsService userDetailsService;

    public JwtUtil(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.expiration_time.access_token}") long accessTokenExprTime,
        @Value("${jwt.expiration_time.refresh_token}") long refreshTokenExprTime,
        @Qualifier("principalDetailsServiceImpl") UserDetailsService userDetailsService
    ) {
        this.SECRET_KEY = secretKey;
        this.ACCESS_TOKEN_EXPIRATION_TIME = accessTokenExprTime;
        this.REFRESH_TOKEN_EXPIRATION_TIME = refreshTokenExprTime;
        this.userDetailsService = userDetailsService;
    }

    /***
     * @param secretKey : yml 에 저장되어 있는 secret key
     * @return : secret key 를 인코딩 하여 Key 객체로 리턴
     */
    private SecretKey getSigningKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰을 파싱하여 토큰에 들어있는 Claim을 리턴
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey(SECRET_KEY))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰속(claim)에 있는 클라이언트의 id(pk) 리턴
     */
    public Long getIdInToken(String token) {
        return extractAllClaims(token).get(ID, Long.class);
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰을 이용하여 로그인 된 UPA 객체를 가져옴 -> UPA 객체 안에 유저의 권한들이 담겨 있음
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(
            String.valueOf(getIdInToken(token)));
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    /***
     * @param subject : jwt 의 subject 값
     * @param tokenType : AccessToken or RefreshToken
     * @return : 암호화된 JWT
     */
    public String createToken(Long subject, TokenType tokenType) {
        long expirationTime = getExpirationTime(tokenType);

        return Jwts.builder()
            .subject(String.valueOf(subject))
            .claim(ID, subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expirationTime))
            .id(UUID.randomUUID().toString())
            .signWith(getSigningKey(SECRET_KEY), SIG.HS256)
            .compact();
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰 문자열 앞의 Bearer 을 제거하고 토큰 문자열만 리턴
     */
    public String resolveToken(String token) {
        if (token == null) {
            return "";
        }
        return token.substring(BEARER.length());
    }

    public TokenClaimInfo getTokenClaims(String token) {
        return TokenClaimInfo.builder()
            .id(getIdInToken(token))
            .build();
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰의 유효기간이 얼마나 남았는지 리턴
     */
    public Long getExpiration(String token) {
        Date expiration = Jwts.parser()
            .verifyWith(getSigningKey(SECRET_KEY))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();

        long now = System.currentTimeMillis();
        return expiration.getTime() - now;
    }

    private long getExpirationTime(TokenType tokenType) {
        return tokenType == TokenType.ACCESS_TOKEN
            ? ACCESS_TOKEN_EXPIRATION_TIME
            : REFRESH_TOKEN_EXPIRATION_TIME;
    }
}
