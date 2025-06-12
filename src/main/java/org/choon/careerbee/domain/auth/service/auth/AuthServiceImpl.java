package org.choon.careerbee.domain.auth.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
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

    @Override
    public OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider, String origin) {
        OAuthProvider provider = OAuthProvider.fromString(oauthProvider);

        return new OAuthLoginUrlResp(
            providerFactory.getProvider(provider).getLoginUrlByOrigin(origin));
    }

    public AuthTokens login(OAuthLoginParams params, String origin) {
        OAuthInfoResponse info = requestOAuthInfoService.request(params, origin);
        final Member validMember = findOrCreateMember(info);

        tokenRepository.findByMemberAndStatus(validMember, TokenStatus.LIVE)
            .ifPresent(Token::revoke);

        String accessToken = jwtUtil.createToken(validMember.getId(), TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(validMember.getId(), TokenType.REFRESH_TOKEN);

        tokenRepository.save(new Token(validMember, TokenStatus.LIVE, refreshToken));

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        String resolveAccessToken = jwtUtil.resolveToken(accessToken);
        TokenClaimInfo tokenClaims = jwtUtil.getTokenClaims(resolveAccessToken);

        Member memberRef = memberQueryService.getReferenceById(tokenClaims.id());

        tokenRepository.findByMemberIdAndStatus(memberRef.getId(), TokenStatus.LIVE)
            .ifPresent(Token::logout);

        tokenRepository.save(new Token(memberRef, TokenStatus.BLACK, resolveAccessToken));
    }

    @Override
    @Transactional(noRollbackFor = CustomException.class)
    public AuthTokens reissue(String rtInCookie) {
        TokenClaimInfo tokenClaims;
        try {
            tokenClaims = jwtUtil.getTokenClaims(rtInCookie);
        } catch (ExpiredJwtException e) {
            tokenRepository.findByTokenValueAndStatus(rtInCookie, TokenStatus.LIVE)
                .ifPresent(Token::expire);

            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_EXPIRED);
        }

        Member memberRef = memberQueryService.getReferenceById(tokenClaims.id());

        Token currentLiveRt = tokenRepository
            .findByTokenValueAndStatus(rtInCookie, TokenStatus.LIVE)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND));

        AuthTokens newTokens = tokenGenerator.generateToken(memberRef.getId());
        tokenRepository.save(new Token(memberRef, TokenStatus.LIVE, newTokens.refreshToken()));

        currentLiveRt.revoke();

        return newTokens;
    }

    private Member findOrCreateMember(OAuthInfoResponse info) {
        try {
            return memberQueryService
                .findMemberByProviderId(info.getProviderId())
                .orElseGet(() -> memberCommandService.forceJoin(info));
        } catch (DataIntegrityViolationException ex) {
            return memberQueryService
                .findMemberByProviderId(info.getProviderId())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.INVALID_LOGIN_LOGIC));
        }
    }

}
