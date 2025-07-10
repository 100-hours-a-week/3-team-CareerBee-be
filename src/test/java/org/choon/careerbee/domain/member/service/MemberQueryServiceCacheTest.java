package org.choon.careerbee.domain.member.service;

import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MemberQueryServiceCacheTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @MockitoBean
    private MemberRepository memberRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache myInfoCache = cacheManager.getCache("myInfo");
        if (myInfoCache != null) {
            myInfoCache.clear();
        }
    }

    // Todo : 내 정보 조회에서 캐싱 다시 적용하고 테스트 재작성 필요
//    @Test
//    @DisplayName("내 정보 조회 - 첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시를 사용한다")
//    void getMyInfoByMemberId_shouldUseCacheOnSecondCall() {
//        // given
//        Long memberId = 1L;
//        MyInfoResp expectedResponse = new MyInfoResp(
//            "testNick", "test@com.bet", "test.url", true, 100
//        );
//
//        when(memberRepository.fetchMyInfoByMemberId(memberId))
//            .thenReturn(expectedResponse);
//
//        // when
//        MyInfoResp result1 = memberQueryService.getMyInfoByMemberId(memberId);
//
//        // then
//        assertThat(result1.nickname()).isEqualTo("testNick");
//        verify(memberRepository, times(1)).fetchMyInfoByMemberId(memberId);
//
//        // when
//        MyInfoResp result2 = memberQueryService.getMyInfoByMemberId(memberId);
//
//        // then
//        assertThat(result2.nickname()).isEqualTo("testNick");
//        verify(memberRepository, times(1)).fetchMyInfoByMemberId(memberId);
//    }
}
