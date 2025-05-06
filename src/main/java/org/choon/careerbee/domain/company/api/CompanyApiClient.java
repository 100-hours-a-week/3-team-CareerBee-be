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

    public SaraminRecruitingResp searchJobs() {
        return saraminRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/job-search")
                .queryParam("access-key", props.getAccessKey())
                .queryParam("keywords", "it")
                .queryParam("bbs_gb", 1)
                .queryParam("loc_cd", 102180)
                .queryParam("ind_cd", 3)
                .queryParam("count", 50)
                .queryParam("fields", "posting-date,expiration-date")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .body(SaraminRecruitingResp.class);
    }

}
