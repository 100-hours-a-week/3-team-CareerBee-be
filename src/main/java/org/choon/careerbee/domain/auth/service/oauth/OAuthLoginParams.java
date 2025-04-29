package org.choon.careerbee.domain.auth.service.oauth;

import org.choon.careerbee.domain.auth.enums.OAuthProvider;
import org.springframework.util.MultiValueMap;

public interface OAuthLoginParams {
    /***
     * 카카오(OAuth) 요청에 필요한 데이터를 가지고있는 파라미터
     * 해당 인터페이스의 구현체는 Controller의 @RequestBody로도 사용되므로 get_ 네이밍을 사용해선 안된다.
     */
    OAuthProvider oauthProvider();
    MultiValueMap<String, String> makeBody();
}
