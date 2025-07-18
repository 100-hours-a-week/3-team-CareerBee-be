package org.choon.careerbee.domain.auth.security;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.auth.dto.internal.MemberAuthInfo;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrincipalDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        MemberAuthInfo validMemberAuthInfo = memberRepository.getMemberAuthInfo(Long.valueOf(id));
        if (validMemberAuthInfo == null) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }

        return new PrincipalDetails(validMemberAuthInfo);
    }
}
