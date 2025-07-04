package org.choon.careerbee.domain.company.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.choon.careerbee.domain.company.exception.RetryableSaraminException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class CompanyApiClient {

    private final RestClient saraminRestClient;
    private final SaraminApiProperties props;
    private final ObjectMapper objectMapper;

    public CompanyApiClient(
        @Qualifier("saraminRestClient") RestClient saraminRestClient,
        SaraminApiProperties props,
        ObjectMapper objectMapper
    ) {
        this.saraminRestClient = saraminRestClient;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public SaraminRecruitingResp searchAllRecruitment(String keyword) {
        ResponseEntity<String> response = saraminRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/job-search")
                .queryParam("access-key", props.getAccessKey())
                .queryParam("keywords", keyword) // 검색 키워드
                .queryParam("loc_cd", 102180) // 근무지/지역조건
                .queryParam("job_mid_cd", 2) // 상위 직무 코드
                .queryParam("count", 110) // 검색 결과 수
                .queryParam("fields", "posting-date,expiration-date") // 공고 시작, 마감일
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .toEntity(String.class);

        return handleSaraminResponse(response);
    }

    public SaraminRecruitingResp searchOpenRecruitment(String keyword) {
        ResponseEntity<String> response = saraminRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/job-search")
                .queryParam("access-key", props.getAccessKey())
                .queryParam("keywords", keyword)
                .queryParam("bbs_gb", 1)
                .queryParam("loc_cd", 102180)
                .queryParam("job_mid_cd", 2)
                .queryParam("count", 110)
                .queryParam("fields", "posting-date,expiration-date")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .toEntity(String.class);

        return handleSaraminResponse(response);
    }

    private SaraminRecruitingResp handleSaraminResponse(ResponseEntity<String> response) {
        String body = response.getBody();

        try {
            JsonNode root = objectMapper.readTree(body);
            int code = root.path("code").asInt();

            if (code == 99) {
                throw new RetryableSaraminException("사람인 일시적 오류 (code 99)");
            }

            Map<Integer, CustomResponseStatus> errorCodeMap = Map.of(
                1, CustomResponseStatus.SARAMIN_API_KEY_EMPTY_ERROR,
                2, CustomResponseStatus.SARAMIN_API_KEY_INVALID_ERROR,
                3, CustomResponseStatus.SARAMIN_INVALID_PARAM_ERROR,
                4, CustomResponseStatus.SARAMIN_TOO_MANY_REQUEST_ERROR
            );

            if (code != 0) {
                CustomResponseStatus status = errorCodeMap.getOrDefault(
                    code, CustomResponseStatus.INTERNAL_SERVER_ERROR
                );
                throw new CustomException(status);
            }

            return objectMapper.treeToValue(root, SaraminRecruitingResp.class);

        } catch (JsonProcessingException e) {
            throw new CustomException(CustomResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
