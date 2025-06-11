package org.choon.careerbee.domain.auth.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.entity.Token;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.auth.entity.enums.TokenStatus;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.auth.repository.TokenRepository;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginParams;
import org.choon.careerbee.domain.auth.service.oauth.OAuthLoginUrlProviderFactory;
import org.choon.careerbee.domain.auth.service.oauth.RequestOAuthInfoService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.member.service.MemberCommandService;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.choon.careerbee.util.jwt.TokenGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final TokenGenerator tokenGenerator;
    private final OAuthLoginUrlProviderFactory providerFactory;
    private final RequestOAuthInfoService requestOAuthInfoService;

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    private final TokenRepository tokenRepository;

    private final MemberRepository memberRepository; // Todo : 추후 Redis로 이전시 없앨 예정. (개발 편의성을 위해 둠)

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
        String refreshToken = tokenRepository
            .findByMemberAndStatus(validMember, TokenStatus.LIVE)
            .map(Token::getTokenValue)
            .orElseGet(() -> {
                String newRt = jwtUtil.createToken(validMember.getId(), TokenType.REFRESH_TOKEN);
                tokenRepository.save(new Token(validMember, TokenStatus.LIVE, newRt));
                return newRt;
            });

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        String resolveAccessToken = jwtUtil.resolveToken(accessToken);
        TokenClaimInfo tokenClaims = jwtUtil.getTokenClaims(resolveAccessToken);

        Member member = memberQueryService.findById(tokenClaims.id());
        Token refreshTokenInRDB = tokenRepository
            .findByMemberIdAndStatus(tokenClaims.id(), TokenStatus.LIVE)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND));

        refreshTokenInRDB.logout();
        tokenRepository.save(new Token(member, TokenStatus.BLACK, resolveAccessToken));
    }

    @Override
    public AuthTokens reissue(String refreshToken) {
        TokenClaimInfo tokenClaims;
        try {
            tokenClaims = jwtUtil.getTokenClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_EXPIRED);
        }

        Member findMember = memberQueryService.findById(tokenClaims.id());
        Token refreshTokenInRDB = tokenRepository
            .findByMemberIdAndStatus(findMember.getId(), TokenStatus.LIVE)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND));

        if (!Objects.equals(refreshToken, refreshTokenInRDB.getTokenValue())) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_MATCH);
        }

        AuthTokens generateTokens = tokenGenerator.generateToken(findMember.getId());
        refreshTokenInRDB.revoke();

        tokenRepository.save(
            new Token(findMember, TokenStatus.LIVE, generateTokens.refreshToken())
        );
        return generateTokens;
    }

    private Member findOrCreateMember(OAuthInfoResponse info) {
        try {
            return memberRepository
                .findByProviderId(info.getProviderId())
                .orElseGet(() -> memberCommandService.forceJoin(info));
        } catch (DataIntegrityViolationException ex) {
            return memberRepository
                .findByProviderId(info.getProviderId())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.INVALID_LOGIN_LOGIC));
        }
    }

}
