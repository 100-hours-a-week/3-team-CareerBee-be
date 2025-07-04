package org.choon.careerbee.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Override
    @Cacheable(cacheNames = "myInfo", key = "#memberId", unless = "#result == null")
    public MyInfoResp getMyInfoByMemberId(Long memberId) {
        return memberRepository.fetchMyInfoByMemberId(memberId);
    }

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

    @Override
    public Member getReferenceById(Long memberId) {
        try {
            return memberRepository.getReferenceById(memberId);
        } catch (EntityNotFoundException e) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }
    }

    @Override
    public Optional<Member> findMemberByProviderId(Long providerId) {
        return memberRepository.findByProviderId(providerId);
    }

    @Override
    public String getNicknameByMemberId(Long memberId) {
        return memberRepository.getNicknameByMemberId(memberId)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

    @Override
    public List<Long> findAllMemberIds() {
        return memberRepository.findAllMemberIds();
    }

}
