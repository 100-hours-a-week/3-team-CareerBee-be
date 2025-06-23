package org.choon.careerbee.domain.competition.service.summary;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableRetry
@ActiveProfiles("test")
class CompetitionSummaryRetryTest {

    @MockitoBean
    CompetitionResultRepository resultRepository;
    @MockitoBean
    CompetitionSummaryRepository summaryRepository;
    @MockitoBean
    MemberQueryService memberQueryService;
    @MockitoBean
    ApplicationEventPublisher eventPublisher;

    @Autowired
    CompetitionSummaryService service;   // 프록시된 Bean 주입

    @Test
    void dailySummary_예외두번후성공_재시도3회확인() {
        // given
        LocalDate today = LocalDate.now();
        when(resultRepository.fetchResultSummaryOfDaily(today))
            .thenReturn(List.of(new DailyResultSummaryResp(1L, (short) 5, 123)));

        when(memberQueryService.getNicknameByMemberId(1L)).thenReturn("Tester");

        // summaryRepository.rewritePeriod()가 처음 두 번은 예외,
        // 세 번째는 정상 종료하도록 설정
        AtomicInteger callCnt = new AtomicInteger();
        willAnswer(inv -> {
            if (callCnt.getAndIncrement() < 2) {
                throw new TransientDataAccessException("stub") {
                };
            }
            return null;
        }).given(summaryRepository)
            .rewritePeriod(eq(SummaryType.DAY), eq(today), eq(today), anyList());

        // when
        service.dailySummary(today);

        // then
        then(summaryRepository)
            .should(times(3))
            .rewritePeriod(eq(SummaryType.DAY), eq(today), eq(today), anyList());
    }

    @Test
    void dailySummary_3회모두실패_Recover메서드호출() {
        // given
        LocalDate today = LocalDate.now();
        given(resultRepository.fetchResultSummaryOfDaily(today))
            .willReturn(List.of(new DailyResultSummaryResp(1L, (short) 5, 123)));
        given(memberQueryService.getNicknameByMemberId(1L)).willReturn("Tester");

        // 3회 모두 예외 발생
        willThrow(new TransientDataAccessException("always fail") {
        })
            .given(summaryRepository)
            .rewritePeriod(eq(SummaryType.DAY), eq(today), eq(today), anyList());

        // when & then
        assertThatCode(() -> service.dailySummary(today))
            .doesNotThrowAnyException();

        then(summaryRepository).should(times(3))
            .rewritePeriod(eq(SummaryType.DAY), eq(today), eq(today), anyList());
    }
}
