package org.codequistify.master.domain.lab.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.domain.stage.dto.StageActionRequest;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class LabAssignmentService {
    private final RestTemplate restTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(LabAssignmentService.class);
    private final String NAMESPACE = "default";

    @LogExecutionTime
    public ResponseEntity<SuccessResponse> sendGradingRequest(String stageCode, String uid, StageActionRequest request) {
        String svcName = KubernetesResourceNaming.getServiceName(stageCode, uid);
        String url = KubernetesResourceNaming.getServiceDNS(svcName, NAMESPACE) + "/grade";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<StageActionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<SuccessResponse> response = restTemplate.postForEntity(url, entity, SuccessResponse.class);
        if (response.getStatusCode().is5xxServerError()) {
            LOGGER.info("[sendGradingRequest] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
            throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @LogExecutionTime
    public ResponseEntity<SuccessResponse> sendComposeRequest(String stageCode, String uid, StageActionRequest request) {
        String svcName = KubernetesResourceNaming.getServiceName(stageCode, uid);
        String url = KubernetesResourceNaming.getServiceDNS(svcName, NAMESPACE) + "/compose";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<StageActionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<SuccessResponse> response = restTemplate.postForEntity(url, entity, SuccessResponse.class);
        if (response.getStatusCode().is5xxServerError()) {
            LOGGER.info("[sendComposeRequest] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
            throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
