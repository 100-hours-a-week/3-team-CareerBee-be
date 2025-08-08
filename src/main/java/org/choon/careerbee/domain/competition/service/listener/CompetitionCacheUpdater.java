package org.choon.careerbee.domain.competition.service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.event.CompetitionJoinedEvent;
import org.choon.careerbee.util.date.TimeUtil;
import org.choon.careerbee.util.redis.RedisKeyFactory;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CompetitionCacheUpdater {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionJoined(CompetitionJoinedEvent event) {
        RBucket<String> compParticipationBucket = redissonClient.getBucket(
            RedisKeyFactory.compParticipationKey(
                event.competitionId(),
                event.memberId()
            )
        );

        try {
            compParticipationBucket.set(
                objectMapper.writeValueAsString(true),
                Duration.ofSeconds(TimeUtil.getSecondsUntilMidnight())
            );
        } catch (JsonProcessingException e) {
            throw new CustomException(CustomResponseStatus.JSON_PARSING_ERROR);
        }
    }

}
