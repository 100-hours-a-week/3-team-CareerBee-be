package org.choon.careerbee.domain.auth.service.oauth;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthLoginUrlProviderFactory {

    private final List<OAuthLoginUrlProvider> providers;

    public OAuthLoginUrlProvider getProvider(OAuthProvider provider) {
        return providers.stream()
            .filter(p -> p.getOAuthProvider() == provider)
            .findFirst()
            .orElseThrow(() -> new CustomException(CustomResponseStatus.OAUTH_PROVIDER_NOT_EXIST));
    }
}
