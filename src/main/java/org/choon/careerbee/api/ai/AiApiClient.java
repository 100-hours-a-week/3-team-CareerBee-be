package org.choon.careerbee.api.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiApiClient {

    @Qualifier("aiRestClient")
    private final RestClient aiRestClient;

    public ResumeDraftResp requestResumeDraft(ResumeDraftReq resumeDraftReq) {
        ResumeDraftResp body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/resume/draft")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(resumeDraftReq)
            .retrieve()
            .body(ResumeDraftResp.class);

        log.info("response : \n{}", body);

        return body;
    }

}
