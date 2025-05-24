package org.choon.careerbee.domain.auth.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
import org.choon.careerbee.domain.auth.dto.jwt.TokenClaimInfo;
import org.choon.careerbee.domain.auth.dto.response.LoginResp.UserInfo;
import org.choon.careerbee.domain.auth.dto.response.OAuthLoginUrlResp;
import org.choon.careerbee.domain.auth.dto.response.TokenAndUserInfo;
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

    @Override
    public TokenAndUserInfo login(OAuthLoginParams oAuthLoginParams, String origin) {
        OAuthInfoResponse oAuthInfo = requestOAuthInfoService.request(oAuthLoginParams, origin);

        // Todo : 추후(Redis로 변경)에는 Member 엔티티 보단 memberId만 있으면 되므로 리팩토링시 변경 코드
        // Todo : forceJoin의 리턴타입도 Long으로 저장된 member의 id를 리턴해줘야함.
        Member member = memberRepository.findByEmail(oAuthInfo.getEmail())
            .orElseGet(() -> memberCommandService.forceJoin(oAuthInfo));

        String accessToken = jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        Optional<String> existingRefreshToken = tokenRepository
            .findByMemberAndStatus(member, TokenStatus.LIVE)
            .map(Token::getTokenValue);

        String refreshToken = existingRefreshToken.orElseGet(() -> {
            String newRefreshToken = jwtUtil.createToken(member.getId(), TokenType.REFRESH_TOKEN);
            tokenRepository.save(new Token(member, TokenStatus.LIVE, newRefreshToken));
            return newRefreshToken;
        });

        // Todo : 추후 새 알림이 있다면 해당 코드 변경
        return new TokenAndUserInfo(
            new AuthTokens(accessToken, refreshToken),
            new UserInfo(member.getPoints(), false)
        );
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

}
