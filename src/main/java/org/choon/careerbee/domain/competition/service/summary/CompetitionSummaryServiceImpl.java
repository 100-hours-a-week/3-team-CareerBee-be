package org.choon.careerbee.domain.competition.service.summary;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CompetitionSummaryServiceImpl implements CompetitionSummaryService {

    private final CompetitionSummaryRepository summaryRepository;
    private final CompetitionResultRepository resultRepository;

    @Override
    public void dailySummary(LocalDate now) {
        List<DailyResultSummaryResp> dailyResultSummaryList = resultRepository
            .fetchResultSummaryOfDaily(now);
        if (dailyResultSummaryList.isEmpty()) return;

        List<CompetitionSummary> summaries = IntStream.range(0, dailyResultSummaryList.size())
            .mapToObj(i -> {
                DailyResultSummaryResp summary = dailyResultSummaryList.get(i);
                long rank = i + 1L;
                return CompetitionSummary.of(
                    Member.ofId(summary.memberId()),
                    summary.solvedSum(),
                    Long.valueOf(summary.timeSum()),
                    rank,
                    0,
                    0.0,
                    SummaryType.DAY,
                    now, now);
            })
            .toList();

        summaryRepository.saveAll(summaries);

        // Todo : 이후 오늘의 1등에 대한 알림 발송 로직 필요
    }

    @Override
    public void weekAndMonthSummary(LocalDate now, SummaryType summaryType) {

    }
}
