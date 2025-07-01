package org.choon.careerbee.domain.auth.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProviderFactory;
import org.choon.careerbee.domain.auth.service.oauth.RequestOAuthInfoService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.choon.careerbee.util.jwt.TokenGenerator;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.expiration_time.refresh_token}")
    private Long refreshTokenTTL;

    private static final String RT_KEY = "rt:";
    private static final String BL_KEY = "bl:";

    private final JwtUtil jwtUtil;
    private final TokenGenerator tokenGenerator;
    private final OAuthLoginUrlProviderFactory providerFactory;
    private final RequestOAuthInfoService requestOAuthInfoService;

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    private final RedissonClient redissonClient;

    @Override
    public OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider, String origin) {
        OAuthProvider provider = OAuthProvider.fromString(oauthProvider);

        return new OAuthLoginUrlResp(
            providerFactory.getProvider(provider).getLoginUrlByOrigin(origin));
    }

    public AuthTokens login(OAuthLoginParams params, String origin) {
        OAuthInfoResponse info = requestOAuthInfoService.request(params, origin);
        final Member validMember = findOrCreateMember(info);

        String accessToken = jwtUtil.createToken(validMember.getId(), TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(validMember.getId(), TokenType.REFRESH_TOKEN);

        RBucket<String> bucket = redissonClient.getBucket(RT_KEY + validMember.getId());
        bucket.set(refreshToken, Duration.ofMillis(refreshTokenTTL));

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        String resolveAccessToken = jwtUtil.resolveToken(accessToken);
        TokenClaimInfo tokenClaims = jwtUtil.getTokenClaims(resolveAccessToken);

        Member memberRef = memberQueryService.getReferenceById(tokenClaims.id());

        redissonClient.getBucket(RT_KEY + memberRef.getId()).delete();

        long remainMs = jwtUtil.getRemainingMillis(resolveAccessToken);
        if (remainMs > 0) {
            redissonClient.getBucket(BL_KEY + resolveAccessToken)
                .set("", Duration.ofMillis(remainMs));
        }
    }

    @Override
    @Transactional(noRollbackFor = CustomException.class)
    public AuthTokens reissue(String rtInCookie) {
        TokenClaimInfo tokenClaims = parseTokenOrThrow(rtInCookie);
        log.info("쿠키안에 들어있는 RT : {}", rtInCookie);

        Member memberRef = memberQueryService.getReferenceById(tokenClaims.id());

        RBucket<String> bucket = redissonClient.getBucket(RT_KEY + memberRef.getId());
        String storedRt = bucket.get();

        if (storedRt == null || !storedRt.equals(rtInCookie)) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND);
        }

        AuthTokens newTokens = tokenGenerator.generateToken(memberRef.getId());
        bucket.set(newTokens.refreshToken(), Duration.ofMillis(refreshTokenTTL));

        return newTokens;
    }

    private TokenClaimInfo parseTokenOrThrow(String token) {
        try {
            return jwtUtil.getTokenClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * providerId 로 회원을 찾거나(탈퇴 포함) 없으면 가입한다. - 탈퇴 회원이면 410 Gone 예외 - 동시 가입 레이스가 일어나면 한 번 더 조회
     */
    private Member findOrCreateMember(OAuthInfoResponse info) {
        return memberQueryService.findMemberByProviderId(info.getProviderId())
            .map(this::ensureNotWithdrawn)
            .orElseGet(() -> safelyForceJoin(info));
    }

    /**
     * 탈퇴 회원이면 예외를 던지고, 아니면 그대로 반환
     */
    private Member ensureNotWithdrawn(Member member) {
        if (member.isWithDrawn()) {
            throw new CustomException(CustomResponseStatus.WITHDRAWAL_MEMBER);
        }
        return member;
    }

    /**
     * forceJoin() 과정에서 UNIQUE 제약(동일 providerId) 충돌 시 재조회해서 방금 만들어진 회원을 가져온다.
     */
    private Member safelyForceJoin(OAuthInfoResponse info) {
        try {
            return memberCommandService.forceJoin(info);
        } catch (DataIntegrityViolationException e) {
            return memberQueryService.findMemberByProviderId(info.getProviderId())
                .orElseThrow(() ->
                    new CustomException(CustomResponseStatus.INVALID_LOGIN_LOGIC));
        }
    }

}
