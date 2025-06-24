package org.choon.careerbee.domain.auth.security;

import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.auth.dto.internal.MemberAuthInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails {

    private final MemberAuthInfo authInfo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authInfo.role().toString()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    public Long getId() {
        return authInfo.memberId();
    }
}
