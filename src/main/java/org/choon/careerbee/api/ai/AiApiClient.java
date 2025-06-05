package org.choon.careerbee.api.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.image.dto.request.ExtractResumeReq;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class AiApiClient {

    @Qualifier("aiRestClient")
    private final RestClient aiRestClient;

    public AiApiClient(
        @Qualifier("aiRestClient") RestClient aiRestClient
    ) {
        this.aiRestClient = aiRestClient;
    }

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

        logJson("[1️⃣] 이력서 초안 생성 응답 : \n{}", body);

        return body;
    }

    public ExtractResumeResp requestExtractResume(ExtractResumeReq extractResumeReq) {
        ExtractResumeResp body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/resume/extract")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(extractResumeReq)
            .exchange((req, resp) -> {
                if (resp.getStatusCode().is4xxClientError()) {
                    throw new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST);
                } else if (resp.getStatusCode().is5xxServerError()) {
                    throw new CustomException(CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                } else {
                    ObjectMapper objectMapper1 = new ObjectMapper();
                    logJson("[1️⃣] 이력서 정보 추출 응답", resp.getBody().toString());

                    return objectMapper1.readValue(resp.getBody(), ExtractResumeResp.class);
                }
            });

        logJson("[2️⃣] 최종 이력서 정보 추출 응답", body);

        return body;
    }

    private void logJson(String label, Object body) {
        try {
            String prettyJson = objectMapper.writeValueAsString(body);
            log.info("{}:\n{}", label, prettyJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to format JSON response: {}", e.getMessage());
        }
    }

}
