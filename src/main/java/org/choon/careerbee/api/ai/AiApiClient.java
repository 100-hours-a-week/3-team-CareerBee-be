package org.choon.careerbee.api.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.image.dto.request.ExtractResumeReq;
import org.choon.careerbee.domain.interview.dto.request.AiFeedbackReq;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.interview.dto.response.AiFeedbackRespWrapper;
import org.choon.careerbee.domain.member.dto.internal.AdvancedResumeInitReq;
import org.choon.careerbee.domain.member.dto.internal.AdvancedResumeInitRespFromAI;
import org.choon.careerbee.domain.member.dto.internal.AdvancedResumeRespFromAi;
import org.choon.careerbee.domain.member.dto.internal.ExtractResumeRespFromAi;
import org.choon.careerbee.domain.member.dto.request.AdvancedResumeUpdateReqToAi;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;
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

    public ExtractResumeRespFromAi requestExtractResume(ExtractResumeReq extractResumeReq) {
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
                    logJson("1. 이력서 정보 추출 응답", responseBody);
                    return objectMapper.readValue(responseBody, AiResumeExtractResp.class);
                }
            });

        logJson("2. 최종 이력서 정보 추출 응답", body);
        return objectMapper.convertValue(body.data(), ExtractResumeRespFromAi.class);
    }

    public CompletableFuture<ExtractResumeResp> requestExtractResumeAsync(
        ExtractResumeReq extractResumeReq
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("요청 객체 : {}", extractResumeReq);

                // 동기 방식으로 한 번 실행하지만 supplyAsync로 비동기화
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
                            throw new CustomException(
                                CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                        } else {
                            logJson("1. 이력서 정보 추출 응답", responseBody);
                            return objectMapper.readValue(responseBody, AiResumeExtractResp.class);
                        }
                    });

                logJson("2. 최종 이력서 정보 추출 응답", body);

                ExtractResumeRespFromAi extractResumeRespFromAi = objectMapper.convertValue(
                    body.data(), ExtractResumeRespFromAi.class);

                return ExtractResumeResp.from(extractResumeRespFromAi);
            } catch (Exception e) {
                // 예외는 비동기적으로 처리되므로 런타임 예외로 감싸서 throw
                throw new RuntimeException("AI 서버 요청 실패", e);
            }
        });
    }

    public AdvancedResumeInitResp requestAdvancedResumeInit(
        AdvancedResumeInitReq extractResumeReq
    ) {
        log.info("요청 객체 :  {}", extractResumeReq);
        AdvancedResumeInitRespFromAI body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/resume/agent/init")
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
                    logJson("1. 고급 이력서 생성(init) 응답", responseBody);
                    return objectMapper.readValue(responseBody, AdvancedResumeInitRespFromAI.class);
                }
            });

        logJson("2. 고급 이력서 생성(init) 응답", body);
        return objectMapper.convertValue(body, AdvancedResumeInitResp.class);
    }

    public CompletableFuture<AdvancedResumeInitResp> requestAdvancedResumeInitAsync(
        AdvancedResumeInitReq extractResumeReq
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("요청 객체 :  {}", extractResumeReq);
                AdvancedResumeInitRespFromAI body = aiRestClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/resume/agent/init")
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
                            throw new CustomException(
                                CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                        } else {
                            logJson("1. 고급 이력서 생성(init) 응답", responseBody);
                            return objectMapper.readValue(responseBody,
                                AdvancedResumeInitRespFromAI.class);
                        }
                    });

                logJson("2. 고급 이력서 생성(init) 응답", body);
                return objectMapper.convertValue(body, AdvancedResumeInitResp.class);
            } catch (Exception e) {
                // 예외는 비동기적으로 처리되므로 런타임 예외로 감싸서 throw
                throw new RuntimeException("AI 서버 요청 실패", e);
            }
        });

    }

    public AdvancedResumeRespFromAi requestAdvancedResumeUpdate(
        AdvancedResumeUpdateReqToAi reqToAi
    ) {
        log.info("요청 객체 :  {}", reqToAi);

        AdvancedResumeRespFromAi result = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder.path("/api/v1/resume/agent/update").build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(reqToAi)
            .exchange((req, resp) -> {
                String responseBody = new String(resp.getBody().readAllBytes(),
                    StandardCharsets.UTF_8);

                if (resp.getStatusCode().is4xxClientError()) {
                    log.error("[4xx] ai 서버 에러!! : {}", responseBody);
                    throw new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST);
                } else if (resp.getStatusCode().is5xxServerError()) {
                    log.error("[5xx] ai 서버 에러!! : {}", responseBody);
                    throw new CustomException(CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                }

                log.info("데이터 : {}", responseBody);
                logJson("1. 고급 이력서 생성(update) 응답", responseBody);

                return objectMapper.readValue(responseBody, AdvancedResumeRespFromAi.class);
            });

        logJson("2. 고급 이력서 생성(update) 최종 응답 객체", result);
        return result;
    }

    public CompletableFuture<AdvancedResumeRespFromAi> requestAdvancedResumeUpdateAsync(
        AdvancedResumeUpdateReqToAi reqToAi
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("요청 객체 :  {}", reqToAi);
                AdvancedResumeRespFromAi result = aiRestClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/resume/agent/update").build())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(reqToAi)
                    .exchange((req, resp) -> {
                        String responseBody = new String(resp.getBody().readAllBytes(),
                            StandardCharsets.UTF_8);

                        if (resp.getStatusCode().is4xxClientError()) {
                            log.error("[4xx] ai 서버 에러!! : {}", responseBody);
                            throw new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST);
                        } else if (resp.getStatusCode().is5xxServerError()) {
                            log.error("[5xx] ai 서버 에러!! : {}", responseBody);
                            throw new CustomException(
                                CustomResponseStatus.AI_INTERNAL_SERVER_ERROR);
                        }

                        log.info("데이터 : {}", responseBody);
                        logJson("1. 고급 이력서 생성(update) 응답", responseBody);
                        return objectMapper.readValue(
                            responseBody, AdvancedResumeRespFromAi.class);
                    });

                logJson("2. 고급 이력서 생성(update) 응답", result);
                return result;

            } catch (Exception e) {
                // 예외는 비동기적으로 처리되므로 런타임 예외로 감싸서 throw
                throw new RuntimeException("AI 서버 요청 실패", e);
            }
        });
    }

    public AiFeedbackResp requestFeedback(AiFeedbackReq feedbackReq) {
        AiFeedbackRespWrapper body = aiRestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/feedback/create")
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(feedbackReq)
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
                    logJson("생성된 피드백", responseBody);
                    return objectMapper.readValue(responseBody, AiFeedbackRespWrapper.class);
                }
            });

        return body.data();
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
