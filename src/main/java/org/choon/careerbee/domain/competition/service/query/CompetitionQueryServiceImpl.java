package org.choon.careerbee.domain.competition.service.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CompetitionQueryServiceImpl implements CompetitionQueryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionSummaryRepository competitionSummaryRepository;
    private final CompetitionResultRepository competitionResultRepository;

    private final RedissonClient redissonClient;
    private final Clock clock;

    @Override
    public CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    ) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        String key = getCompetitionParticipantKey(accessMemberId);
        RBucket<Boolean> bucket = redissonClient.getBucket(key,
            new TypedJsonJacksonCodec(Boolean.class, new ObjectMapper()));

        if (bucket.isExists()) {
            log.info("Cache HIT - key: {}", key);
            return new CompetitionParticipationResp(bucket.get());
        }

        // 3. 캐시 없음 (Cache Miss) -> DB 조회
        log.info("Cache MISS - key: {}", key);
        boolean isParticipated = competitionParticipantRepository.existsByMemberIdAndCompetitionId(
            accessMemberId, competitionId);

        // 4. DB 결과를 캐시에 저장 (동적 TTL 계산)
        long secondsUntilMidnight = Duration.between(LocalTime.now(clock), LocalTime.MAX)
            .getSeconds();
        bucket.set(isParticipated, secondsUntilMidnight, TimeUnit.SECONDS);
        log.info("Cache SET - key: {}, value: {}, TTL: {} seconds", key, isParticipated,
            secondsUntilMidnight);

        return new CompetitionParticipationResp(isParticipated);
    }

    @Override
    @Cacheable(cacheNames = "competitionProblem", key = "#competitionId", unless = "#result == null")
    public CompetitionProblemResp fetchProblems(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        return competitionRepository.fetchCompetitionProblemsByCompetitionId(competitionId);
    }

    @Override
    @Cacheable(
        cacheNames = "competitionRank",
        key = "'competition:rank:' + #today.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMdd'))")
    public CompetitionRankingResp fetchRankings(LocalDate today) {
        return competitionSummaryRepository.fetchRankings(today);
    }

    @Override
    @Cacheable(cacheNames = "competitionId", key = "#today", unless = "#result == null")
    public CompetitionIdResp fetchCompetitionIdBy(LocalDate today) {
        return competitionRepository.fetchCompetitionIdFromToday(today);
    }

    @Override
    @Cacheable(cacheNames = "memberRank",
        key = "'member:' + #accessMemberId + ':rank:' + #today.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMdd'))")
    public MemberRankingResp fetchMemberCompetitionRankingById(
        Long accessMemberId, LocalDate today
    ) {
        return competitionSummaryRepository.fetchMemberRankingById(accessMemberId, today);
    }

    @Override
    public MemberLiveRankingResp fetchMemberLiveRanking(
        Long accessMemberId, LocalDate today
    ) {
        return competitionResultRepository.fetchMemberLiveRankingByDate(
            accessMemberId, today
        ).orElse(null);
    }

    @Override
    public LiveRankingResp fetchLiveRanking(LocalDate today) {
        return competitionResultRepository.fetchLiveRankingByDate(today);
    }

    private String getCompetitionParticipantKey(Long memberId) {
        String todayStr = LocalDate.now(clock).format(DATE_FORMATTER);
        return "member:" + memberId + ":participant" + todayStr;
    }
}
