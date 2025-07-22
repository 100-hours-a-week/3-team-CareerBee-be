package org.choon.careerbee.domain.company.api;

import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.company.dto.request.CompanyRevalidateReq;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class NextApiClient {

    private final RestClient nextRestClient;
    private final NextApiProperties nextApiProperties;

    public NextApiClient(
        @Qualifier("nextRestClient") RestClient nextRestClient,
        NextApiProperties nextApiProperties
    ) {
        this.nextRestClient = nextRestClient;
        this.nextApiProperties = nextApiProperties;
    }

    public void revalidateRecentIssue(CompanyRevalidateReq revalidateReq) {
        log.info("req : {}", revalidateReq);
        nextRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/api/revalidate-issue")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(revalidateReq)
            .retrieve()
            .toBodilessEntity();
    }
}
