package org.choon.careerbee.domain.competition.service.summary;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.SummaryPeriod;
import org.choon.careerbee.domain.competition.dto.request.TempSummaryInfo;
import org.choon.careerbee.domain.competition.dto.response.DailyResultSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.DateSummaryResp;
import org.choon.careerbee.domain.competition.dto.response.ResultSummaryResp;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.notification.service.sse.NotificationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CompetitionSummaryServiceImpl implements CompetitionSummaryService {

    private final CompetitionSummaryRepository summaryRepository;
    private final CompetitionResultRepository resultRepository;
    private final MemberQueryService memberQueryService;
    private final NotificationEventPublisher eventPublisher;

    @Override
    public void dailySummary(LocalDate now) {
        List<DailyResultSummaryResp> dailyResultSummaryList = resultRepository
            .fetchResultSummaryOfDaily(now);
        if (dailyResultSummaryList.isEmpty()) {
            log.info("[일일 집계] 집계할 데이터가 존재하지 않습니다.");
            return;
        }

        final AtomicReference<String> firstMemberNick = new AtomicReference<>();
        List<CompetitionSummary> summaries = IntStream.range(0, dailyResultSummaryList.size())
            .mapToObj(i -> {
                DailyResultSummaryResp summary = dailyResultSummaryList.get(i);
                long rank = i + 1L;
                if (rank == 1) {
                    String firstMemberNickname = memberQueryService.getNicknameByMemberId(
                        summary.memberId());
                    firstMemberNick.set(firstMemberNickname);
                }
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

        // 일일 대회 1등 유저 알림 발송
        eventPublisher.sendDailyFirstMemberNoti(
            firstMemberNick.get(),
            memberQueryService.findAllMemberIds()
        );
    }

    @Override
    public void weekAndMonthSummary(SummaryPeriod summaryPeriod, SummaryType summaryType) {
        // 1. 기간 내의 대회 결과 데이터 조회
        List<ResultSummaryResp> resultSummaryList = resultRepository
            .fetchResultSummaryByPeriod(summaryPeriod);
        if (resultSummaryList.isEmpty()) {
            log.info("[주간, 월간 집계] 집계할 데이터가 존재하지 않습니다. 타입 : {}", summaryType);
            return;
        }

        // 2. 최대 연속 참여일 계산하기 위한 데이터 조회
        // 2-1. 집계 기간동안 참여한 유저의 ID 조회
        List<Long> summaryMemberIds = resultSummaryList.stream()
            .map(ResultSummaryResp::memberId)
            .toList();

        // 2-2. 유저의 최대 연속 참여일을 계산하기 위한 데이터 조회
        List<DateSummaryResp> dateSummaryResps = resultRepository.fetchDateSummaryIn(
            summaryPeriod, summaryMemberIds
        );

        // 2-3. 각 유저당 최대 연속 참여일을 Map 형태로 저장
        Map<Long, Integer> maxStreakMap = dateSummaryResps.stream()
            .collect(Collectors.groupingBy(
                DateSummaryResp::memberId,
                Collectors.mapping(DateSummaryResp::createdDateOnly, Collectors.toList())))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> calculateMaxStreak(e.getValue())));

        // 2-4. 위의 데이터를 조합하여 임시 집계 데이터 생성
        List<TempSummaryInfo> tempSummaryInfos = new ArrayList<>();
        for (ResultSummaryResp resultSummaryResp : resultSummaryList) {
            Long memberId = resultSummaryResp.memberId();

            tempSummaryInfos.add(new TempSummaryInfo(
                memberId,
                resultSummaryResp.solvedSum(),
                resultSummaryResp.timeSum(),
                resultSummaryResp.correctRate(),
                maxStreakMap.getOrDefault(memberId, 0)
            ));
        }

        // 3. 기존에 존재하는 집계 데이터 조회
        List<CompetitionSummary> existSummaries = summaryRepository.fetchSummaryByPeriodAndType(
            summaryPeriod, summaryType);

        // 4. Map으로 변환
        Map<Long, CompetitionSummary> existSummaryMap = existSummaries.stream()
            .collect(Collectors.toMap(
                cs -> cs.getMember().getId(),
                Function.identity()
            ));

        // 5. 새롭게 집계한 데이터로 기존 데이터 업데이트
        // 만약 새로운 집계데이터라면 insert
        List<CompetitionSummary> competitionSummaryToInsert = new ArrayList<>();
        for (TempSummaryInfo tempSummaryInfo : tempSummaryInfos) {
            CompetitionSummary competitionSummary = existSummaryMap.get(tempSummaryInfo.memberId());

            if (competitionSummary == null) {
                competitionSummaryToInsert.add(CompetitionSummary.of(
                    Member.ofId(tempSummaryInfo.memberId()),
                    tempSummaryInfo.solvedSum(),
                    tempSummaryInfo.timeSum(),
                    0L,
                    tempSummaryInfo.maxStreak(),
                    tempSummaryInfo.correctRate(),
                    summaryType,
                    summaryPeriod.startAt(),
                    summaryPeriod.endAt()
                ));
                continue;
            }

            competitionSummary.updateSummary(tempSummaryInfo);
        }

        // 6. 랭킹 계산
        // (1) 신규 + 기존 집계 합치기
        List<CompetitionSummary> allSummaries = new ArrayList<>(existSummaryMap.values());
        allSummaries.addAll(competitionSummaryToInsert);

        // (2) 정렬
        allSummaries.sort(Comparator
            .comparingLong(CompetitionSummary::getSolvedCount).reversed()
            .thenComparingLong(CompetitionSummary::getElapsedTime));

        // (3) 랭킹 부여
        long rank = 1L;
        for (CompetitionSummary cs : allSummaries) {
            cs.updateRank(rank++);
        }

        summaryRepository.saveAll(competitionSummaryToInsert);
    }

    private int calculateMaxStreak(List<LocalDate> days) {
        if (days.isEmpty()) {
            return 0;
        }
        days.sort(null);
        int max = 1, cur = 1;
        for (int i = 1; i < days.size(); i++) {
            cur = days.get(i).equals(days.get(i - 1).plusDays(1)) ? cur + 1 : 1;
            max = Math.max(max, cur);
        }
        return max;
    }
}
