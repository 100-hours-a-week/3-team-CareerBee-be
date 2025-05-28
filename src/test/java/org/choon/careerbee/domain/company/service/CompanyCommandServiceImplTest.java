package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyCommandServiceImplTest {

    @InjectMocks
    private CompanyCommandServiceImpl companyCommandService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private CompanyQueryService companyQueryService;

    @Mock
    private WishCompanyRepository wishCompanyRepository;

    @Test
    @DisplayName("관심 회사 등록 - 성공")
    void registWishCompany_success() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            false);

        // when
        companyCommandService.registWishCompany(memberId, companyId);

        // then
        verify(wishCompanyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("관심 회사 등록 - 이미 등록된 경우 예외 발생")
    void registWishCompany_alreadyExists_throwsException() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            true);

        // when & then
        assertThatThrownBy(() -> companyCommandService.registWishCompany(memberId, companyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.WISH_ALREADY_EXIST.getMessage());

        verify(wishCompanyRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("관심 회사 삭제 - 성공")
    void deleteWishCompany_success() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);
        WishCompany mockWishCompany = createWishCompany(mockCompany, mockMember);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.findByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            Optional.of(mockWishCompany));

        // when
        companyCommandService.deleteWishCompany(memberId, companyId);

        // then
        verify(wishCompanyRepository, times(1)).delete(mockWishCompany);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        verify(wishCompanyRepository, times(1))
            .findByMemberAndCompany(memberCaptor.capture(), companyCaptor.capture());
        assertThat(memberCaptor.getValue()).isEqualTo(mockMember);
        assertThat(companyCaptor.getValue()).isEqualTo(mockCompany);
    }

    @Test
    @DisplayName("관심 회사 삭제 - 존재하지 않으면 예외 발생")
    void deleteWishCompany_notFound_throwsException() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.findByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyCommandService.deleteWishCompany(memberId, companyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.WISH_COMPANY_NOT_FOUND.getMessage());

        verify(wishCompanyRepository, times(0)).delete(any());
    }
}
