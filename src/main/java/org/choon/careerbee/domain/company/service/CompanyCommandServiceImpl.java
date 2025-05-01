package org.choon.careerbee.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.repository.CompanyRepository;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {
  private final WishCompanyRepository wishCompanyRepository;
  private final MemberRepository memberRepository;
  private final CompanyRepository companyRepository;

  @Override
  public void registWishCompany(Long accessMemberId, Long companyId) {
    Member validMember = memberRepository.findById(accessMemberId)
        .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

    Company validCompany = companyRepository.findById(companyId)
        .orElseThrow(() -> new CustomException((CustomResponseStatus.COMPANY_NOT_EXIST)));

    if(wishCompanyRepository.existsByMemberAndCompany(validMember, validCompany)) {
      throw new CustomException(CustomResponseStatus.WISH_ALREADY_EXIST);
    }

    wishCompanyRepository.save(WishCompany.of(validMember, validCompany));
  }
}
