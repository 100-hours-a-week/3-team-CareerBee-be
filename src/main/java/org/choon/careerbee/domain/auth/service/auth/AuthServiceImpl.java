package org.choon.careerbee.domain.auth.service.auth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.auth.dto.jwt.AuthTokens;
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
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final JwtUtil jwtUtil;
  private final OAuthLoginUrlProviderFactory providerFactory;
  private final RequestOAuthInfoService requestOAuthInfoService;

  private final MemberCommandService memberCommandService;

  private final TokenRepository tokenRepository;

  private final MemberRepository memberRepository; // Todo : 추후 Redis로 이전시 없앨 예정. (개발 편의성을 위해 둠)


  @Override
  public OAuthLoginUrlResp getOAuthLoginUrl(String oauthProvider) {
    OAuthProvider provider = OAuthProvider.fromString(oauthProvider);

    return new OAuthLoginUrlResp(providerFactory.getProvider(provider).getLoginUrl());
  }

  @Override
  public TokenAndUserInfo login(OAuthLoginParams oAuthLoginParams) {
    OAuthInfoResponse oAuthInfo = requestOAuthInfoService.request(oAuthLoginParams);

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

    return new TokenAndUserInfo(new AuthTokens(accessToken, refreshToken), new UserInfo(member.getPoints(), true));
  }


}
