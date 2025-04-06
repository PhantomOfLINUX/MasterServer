package org.codequistify.master.application.lab.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.stage.dto.StageActionRequest;
import org.codequistify.master.core.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.util.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LabAssignmentService {

    private static final String BASE_URL = "https://lab.pol.or.kr";

    private final Logger logger = LoggerFactory.getLogger(LabAssignmentService.class);
    private final RestTemplate restTemplate;

    @LogExecutionTime
    public SuccessResponse sendGradingRequest(String stageCode, PolId uid, StageActionRequest request) {
        return sendLabRequest("/grade", stageCode, uid, request);
    }

    @LogExecutionTime
    public SuccessResponse sendComposeRequest(String stageCode, PolId uid, StageActionRequest request) {
        return sendLabRequest("/compose", stageCode, uid, request);
    }

    private SuccessResponse sendLabRequest(String path,
                                           String stageCode,
                                           PolId uid,
                                           StageActionRequest request) {

        final String url = buildUrl(path, stageCode, uid);
        final HttpEntity<StageActionRequest> entity = buildRequest(request);

        logger.info("Request URL: {}", url);
        logger.info("Request Payload: {}", request);

        return Optional.of(url)
                       .map(u -> execute(u, entity))
                       .map(this::handleResponse)
                       .orElseThrow();
    }

    private String buildUrl(String path, String stageCode, PolId uid) {
        return BASE_URL + path + KubernetesResourceNaming.getQuery(stageCode, uid);
    }

    private HttpEntity<StageActionRequest> buildRequest(StageActionRequest original) {
        var payload = new StageActionRequest(
                original.stageCode().toLowerCase(),
                original.questionIndex()
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(payload, headers);
    }

    private ResponseEntity<SuccessResponse> execute(String url, HttpEntity<StageActionRequest> entity) {
        try {
            return restTemplate.postForEntity(url, entity, SuccessResponse.class);
        } catch (HttpServerErrorException e) {
            logAndThrow(() -> String.format("서버 오류: %s", e.getResponseBodyAsString()), e);
        } catch (ResourceAccessException e) {
            logAndThrow(() -> String.format("접근 오류: %s", e.getMessage()), e);
        }

        return ResponseEntity.internalServerError().build();
    }

    private SuccessResponse handleResponse(ResponseEntity<SuccessResponse> response) {
        if (response.getStatusCode().is5xxServerError()) {
            throw new ApplicationException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return Optional.ofNullable(response.getBody())
                       .orElseThrow(() -> new ApplicationException(ErrorCode.FAIL_PROCEED,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void logAndThrow(Supplier<String> logMessage, RuntimeException ex) {
        logger.error("[sendLabRequest] {}", logMessage.get());
        throw ex;
    }
}
