package org.choon.careerbee.api.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.image.dto.request.ExtractResumeReq;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.response.AiResumeDraftResp;
import org.choon.careerbee.domain.member.dto.response.AiResumeExtractResp;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private final RestClient aiRestClient;

    public AiApiClient(
        @Qualifier("aiRestClient") RestClient aiRestClient
    ) {
        this.aiRestClient = aiRestClient;
    }

    public ResumeDraftResp requestResumeDraft(ResumeDraftReq resumeDraftReq) {
        AiResumeDraftResp body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/resume/draft")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(resumeDraftReq)
            .retrieve()
            .body(AiResumeDraftResp.class);

        return new ResumeDraftResp(body.data().toString());
    }

    public ExtractResumeResp requestExtractResume(ExtractResumeReq extractResumeReq) {
        log.info("요청 객체 :  {}", extractResumeReq);
        AiResumeExtractResp body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/resume/extract")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(extractResumeReq)
            .exchange((req, resp) -> {
                String responseBody = new String(resp.getBody().readAllBytes(),
                    StandardCharsets.UTF_8);

                if (resp.getStatusCode().is4xxClientError()) {
                    log.error("[4xx] ai 서버 에러!! : {}", responseBody);
                    throw new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST);
                } else if (resp.getStatusCode().is5xxServerError()) {
                    log.error("[5xx] ai 서버 에러!! : {}", responseBody);
                    throw new CustomException(CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                } else {
                    logJson("[1️⃣] 이력서 정보 추출 응답", responseBody);
                    return objectMapper.readValue(responseBody, AiResumeExtractResp.class);
                }
            });

        logJson("[2️⃣] 최종 이력서 정보 추출 응답", body);
        return objectMapper.convertValue(body.data(), ExtractResumeResp.class);
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
