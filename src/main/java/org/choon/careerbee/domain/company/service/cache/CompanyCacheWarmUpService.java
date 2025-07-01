package org.choon.careerbee.domain.company.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.response.CompanyRangeSearchResp.CompanyMarkerInfo;
import org.choon.careerbee.domain.company.service.query.CompanyQueryService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CompanyCacheWarmUpService {

    private static final String GEO_KEY_PREFIX = "company:markerInfo:";

    private final RedissonClient redissonClient;
    private final CompanyQueryService companyQueryService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void warmUpCompanyMarkerInfoCache() {
        List<CompanyMarkerInfo> companyMarkerInfos = companyQueryService.fetchAllCompanyLocations();

        int cachedCount = 0;

        for (CompanyMarkerInfo companyMarkerInfo : companyMarkerInfos) {
            String key = GEO_KEY_PREFIX + companyMarkerInfo.id();
            RBucket<String> bucket = redissonClient.getBucket(key);

            if (!bucket.isExists()) {
                try {
                    String json = objectMapper.writeValueAsString(companyMarkerInfo);
                    bucket.set(json);
                    cachedCount++;
                } catch (JsonProcessingException e) {
                    log.error("캐시 직렬화 실패: {}", companyMarkerInfo, e);
                }
            }
        }

        log.info("총 {}개의 기업 위치 정보를 캐싱했습니다.", cachedCount);
    }

}
