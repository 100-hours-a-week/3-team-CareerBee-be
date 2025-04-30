package org.choon.careerbee.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
