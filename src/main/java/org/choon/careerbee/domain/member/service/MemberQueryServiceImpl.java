package org.choon.careerbee.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Override
    public boolean isMemberExistByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public Optional<Long> getMemberIdByEmail(String email) {
        return memberRepository.findIdByEmail(email);
    }

    @Override
    public MyInfoResp getMyInfoByMemberId(Long memberId) {
        return memberRepository.fetchMyInfoByMemberId(memberId);
    }

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

}
