package org.choon.careerbee.domain.company.api;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.response.SaraminRecruitingResp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CompanyApiClient {

    @Qualifier("saraminRestClient")
    private final RestClient saraminRestClient;

    private final SaraminApiProperties props;

    public SaraminRecruitingResp searchAllRecruitment() {
        return saraminRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/job-search")
                .queryParam("access-key", props.getAccessKey())
                .queryParam("keywords", "it") // 검색 키워드
                .queryParam("loc_cd", 102180) // 근무지/지역조건
                .queryParam("job_mid_cd", 2) // 상위 직무 코드
                .queryParam("count", 110) // 검색 결과 수
                .queryParam("fields", "posting-date,expiration-date") // 공고 시작, 마감일
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .body(SaraminRecruitingResp.class);
    }

}
