package org.choon.careerbee.domain.competition.service.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import io.sentry.Sentry;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.choon.careerbee.config.NoSleepRetryConfig;
import org.choon.careerbee.domain.competition.dto.event.DailyWinnerCalculated;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest
@ActiveProfiles("test")
@Import(NoSleepRetryConfig.class)
@RecordApplicationEvents
class CompetitionSummaryRetryTest {

    @Autowired CompetitionSummaryService service;
    @Autowired ApplicationEvents events;

    @MockitoBean CompetitionResultRepository resultRepository;
    @MockitoBean CompetitionSummaryRepository summaryRepository;
    @MockitoBean MemberQueryService memberQueryService;

    @Spy
    ApplicationEventMulticaster multicaster;

    @Test
    @DisplayName("일일_집계로직_실패시_3회_재시도_예외두번후성공")
    void dailySummary_실패시_재시도_예외두번후성공() {
        LocalDate today = LocalDate.now();
        when(resultRepository.fetchResultSummaryOfDaily(today))
            .thenReturn(List.of(new DailyResultSummaryResp(1L, (short)5, 123)));

        Member winner = createMember("testNick", "test@test.com", 1L);
        when(memberQueryService.findById(1L)).thenReturn(winner);
        when(memberQueryService.findAllMemberIds()).thenReturn(List.of(1L, 2L));

        AtomicInteger c = new AtomicInteger();
        willAnswer(inv -> {
            if (c.getAndIncrement() < 2) throw new TransientDataAccessException("stub") {};
            return null;
        }).given(summaryRepository).batchInsert(anyList());

        // when
        service.dailySummary(today);

        // then
        assertThat(events.stream(DailyWinnerCalculated.class).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("일일집계 - 3회 모두 실패 시 @Recover 실행: 예외 미전파, 이벤트 미발행, Sentry 1회")
    void dailySummary_allFail_triggersRecover_noException_noEvent() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 18);
        Member winner = MemberFixture.createMember("testNick", "test@test.com", 1L);

        when(resultRepository.fetchResultSummaryOfDaily(today))
            .thenReturn(List.of(new DailyResultSummaryResp(1L, (short) 5, 123)));
        when(memberQueryService.findById(1L)).thenReturn(winner);
        when(memberQueryService.findAllMemberIds()).thenReturn(List.of(1L, 2L));

        doThrow(new TransientDataAccessException("always fail") {})
            .when(summaryRepository).batchInsert(anyList());

        try (var sentry = Mockito.mockStatic(Sentry.class)) {
            assertThatCode(() -> service.dailySummary(today))
                .doesNotThrowAnyException();

            then(summaryRepository).should(times(3)).batchInsert(anyList());

            assertThat(events.stream(DailyWinnerCalculated.class).count()).isZero();
            then(multicaster).should(times(0)).multicastEvent(any());

            sentry.verify(() -> Sentry.captureException(any(Throwable.class)), times(1));
        }
    }
}