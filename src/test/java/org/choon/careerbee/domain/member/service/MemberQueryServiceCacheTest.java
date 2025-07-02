package org.choon.careerbee.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.member.dto.response.MyInfoResp;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    // ✅ 실제 Repository 대신 Mock Bean을 주입하여 DB 호출을 흉내 냅니다.
    @MockitoBean
    private MemberRepository memberRepository;

    // ✅ 캐시 상태를 제어하기 위해 CacheManager를 주입받습니다.
    @Autowired
    private CacheManager cacheManager;

    // 각 테스트 실행 전에 "myInfo" 캐시를 깨끗하게 비워 테스트 격리를 보장합니다.
    @BeforeEach
    void setUp() {
        Cache myInfoCache = cacheManager.getCache("myInfo");
        if (myInfoCache != null) {
            myInfoCache.clear();
        }
    }

    @Test
    @DisplayName("내 정보 조회 - 첫 호출에는 DB를 조회하고, 두 번째 호출에는 캐시를 사용한다")
    void getMyInfoByMemberId_shouldUseCacheOnSecondCall() {
        // given
        Long memberId = 1L;
        MyInfoResp expectedResponse = new MyInfoResp(
            "testNick", "test@com.bet", "test.url", true, 100
        );

        when(memberRepository.fetchMyInfoByMemberId(memberId))
            .thenReturn(expectedResponse);

        // when
        MyInfoResp result1 = memberQueryService.getMyInfoByMemberId(memberId);

        // then
        assertThat(result1.nickname()).isEqualTo("testNick");
        verify(memberRepository, times(1)).fetchMyInfoByMemberId(memberId);

        // when
        MyInfoResp result2 = memberQueryService.getMyInfoByMemberId(memberId);

        // then
        assertThat(result2.nickname()).isEqualTo("testNick");
        verify(memberRepository, times(1)).fetchMyInfoByMemberId(memberId);
    }
}
