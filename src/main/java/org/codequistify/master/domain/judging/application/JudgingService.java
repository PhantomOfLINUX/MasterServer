package org.codequistify.master.domain.judging.application;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.judging.domain.vo.JudgingAction;
import org.codequistify.master.domain.judging.domain.vo.JudgingTarget;
import org.codequistify.master.domain.judging.dto.JudgingActionRequest;
import org.codequistify.master.domain.judging.infrastructure.http.LabExternalEndpoints;
import org.codequistify.master.domain.judging.domain.vo.KubernetesResourceName;
import org.codequistify.master.domain.judging.domain.vo.LabUserUid;
import org.codequistify.master.domain.judging.domain.vo.StageCode;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.codequistify.master.global.data.UrlQuery;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class JudgingService {
    private final RestTemplate restTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(JudgingService.class);
    private final String NAMESPACE = "default";


    @Bean
    public void testA() {
        String stageCode = StageImageType.S1015.name();
        String uid = "pol-bdbeej-gj5antzprz";
        KubernetesResourceName resourceName = KubernetesResourceName.of(StageCode.from(stageCode), LabUserUid.from(uid));
        UrlQuery query = resourceName.query();
        System.out.println(LabExternalEndpoints.gradeUrl(query));
        System.out.println(LabExternalEndpoints.composeUrl(query));
    }

    @LogExecutionTime
    public ResponseEntity<SuccessResponse> requestGrading(JudgingTarget target, JudgingAction action) {
        KubernetesResourceName resourceName = KubernetesResourceName.of(target.stageCode(), target.uid());
        String url = LabExternalEndpoints.gradeUrl(resourceName.query());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JudgingActionRequest request = JudgingActionRequest.from(action);

        HttpEntity<JudgingActionRequest> entity = new HttpEntity<>(request, headers);

        // URL 및 요청 데이터 로깅
        LOGGER.info("Request URL: {}", url);
        LOGGER.info("Request Data: {}", request);

        try {
            ResponseEntity<SuccessResponse> response = restTemplate.postForEntity(url, entity, SuccessResponse.class);
            if (response.getStatusCode().is5xxServerError()) {
                LOGGER.info("[requestGrading] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
                throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response;
        } catch (HttpServerErrorException e) {
            // 서버 오류에 대한 상세 정보 로깅
            LOGGER.error("[requestGrading] Internal Server Error: {}, URL: {}", e.getResponseBodyAsString(), url);
            throw e;
        } catch (ResourceAccessException e) {
            // 리소스 접근 오류에 대한 상세 정보 로깅
            LOGGER.error("[requestGrading] Resource Access Error: {}, URL: {}", e.getMessage(), url);
            throw e;
        }
    }

    @LogExecutionTime
    public ResponseEntity<SuccessResponse> requestCompose(JudgingTarget target, JudgingAction action) {
        KubernetesResourceName resourceName = KubernetesResourceName.of(target.stageCode(), target.uid());
        String url = LabExternalEndpoints.composeUrl(resourceName.query());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JudgingActionRequest request = JudgingActionRequest.from(action);
        LOGGER.info("qurl: {}", url);
        HttpEntity<JudgingActionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<SuccessResponse> response = restTemplate.postForEntity(url, entity, SuccessResponse.class);
        if (response.getStatusCode().is5xxServerError()) {
            LOGGER.info("[requestCompose] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
            throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
