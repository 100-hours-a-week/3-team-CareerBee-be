package org.choon.careerbee.domain.company.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.choon.careerbee.fixture.CompanyFixture.createCompany;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.WishCompanyFixture.createWishCompany;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.company.repository.wish.WishCompanyRepository;
import org.choon.careerbee.domain.company.service.command.CompanyCommandServiceImpl;
import org.choon.careerbee.domain.company.service.query.CompanyQueryService;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

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

    @Mock
    private RedissonClient redissonClient;

    @Test
    @DisplayName("관심 회사 등록 - 성공")
    void registWishCompany_success() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);

        // 1. 중복 요청 방지 락 모킹
        RBucket<String> lockBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(lockBucket);
        when(lockBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        // 2. RAtomicLong 모킹
        RAtomicLong mockAtomicLong = mock(RAtomicLong.class);
        // wishCountKey로 getAtomicLong 호출 시, 위에서 만든 mockAtomicLong을 반환하도록 설정
        when(redissonClient.getAtomicLong(anyString())).thenReturn(mockAtomicLong);

        // 3. DB 관련 모킹
        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            false);

        // when
        companyCommandService.registWishCompany(memberId, companyId);

        // then
        // DB에 저장이 1번 호출되었는지 검증
        verify(wishCompanyRepository, times(1)).save(any());
        // 캐시 카운터가 1 증가했는지 검증
        verify(mockAtomicLong, times(1)).incrementAndGet();
    }

    @Test
    @DisplayName("관심 회사 등록 - 이미 등록된 경우 예외 발생")
    void registWishCompany_alreadyExists_throwsException() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);
        RBucket<String> bucket = mock(RBucket.class);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.existsByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            true);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

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

        // 1. 중복 요청 방지 락 모킹
        RBucket<String> lockBucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(lockBucket);
        when(lockBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        // 2. RAtomicLong 모킹
        RAtomicLong mockAtomicLong = mock(RAtomicLong.class);
        when(redissonClient.getAtomicLong(anyString())).thenReturn(mockAtomicLong);
        // 카운터가 존재하고 0보다 크다고 가정 (감소 로직의 if문 통과를 위해)
        when(mockAtomicLong.isExists()).thenReturn(true);
        when(mockAtomicLong.get()).thenReturn(5L); // 0보다 큰 임의의 값

        // 3. DB 관련 모킹
        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.findByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            Optional.of(mockWishCompany));

        // when
        companyCommandService.deleteWishCompany(memberId, companyId);

        // then
        // DB 삭제가 1번 호출되었는지 검증
        verify(wishCompanyRepository, times(1)).delete(mockWishCompany);
        // 캐시 카운터가 1 감소했는지 검증
        verify(mockAtomicLong, times(1)).decrementAndGet();
    }

    @Test
    @DisplayName("관심 회사 삭제 - 존재하지 않으면 예외 발생")
    void deleteWishCompany_notFound_throwsException() {
        // given
        Long memberId = 1L;
        Long companyId = 100L;
        Member mockMember = createMember("testnick", "test@test.com", memberId);
        Company mockCompany = createCompany("테스트 기업", 37.0, 127.0);
        RBucket<String> bucket = mock(RBucket.class);

        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(companyQueryService.findById(companyId)).thenReturn(mockCompany);
        when(wishCompanyRepository.findByMemberAndCompany(mockMember, mockCompany)).thenReturn(
            Optional.empty());
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> companyCommandService.deleteWishCompany(memberId, companyId))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.WISH_COMPANY_NOT_FOUND.getMessage());

        verify(wishCompanyRepository, times(0)).delete(any());
    }
}
