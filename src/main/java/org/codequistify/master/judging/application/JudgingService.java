package org.codequistify.master.judging.application;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.data.Pair;
import org.codequistify.master.global.data.UrlQuery;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.SuccessResponse;
import org.codequistify.master.judging.domain.vo.JudgingAction;
import org.codequistify.master.judging.domain.vo.JudgingTarget;
import org.codequistify.master.judging.infrastructure.http.LabExternalEndpoints;
import org.codequistify.master.judging.presentation.dto.JudgingActionRequest;
import org.codequistify.master.virtualworkspace.application.port.VirtualWorkspaceRepository;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
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
  private final VirtualWorkspaceRepository virtualWorkspaceRepository;
  private final Logger LOGGER = LoggerFactory.getLogger(JudgingService.class);
  private final String NAMESPACE = "default";

  @Bean
  public void testA() {
    WorkspacePublicId publicId = WorkspacePublicId.from("sample-public-id");
    UrlQuery query = publicIdQuery(publicId);
    System.out.println(LabExternalEndpoints.gradeUrl(query));
    System.out.println(LabExternalEndpoints.composeUrl(query));
  }

  @LogExecutionTime
  public ResponseEntity<SuccessResponse> requestGrading(
      JudgingTarget target, JudgingAction action) {
    WorkspacePublicId publicId = loadPublicId(target);
    String url = LabExternalEndpoints.gradeUrl(publicIdQuery(publicId));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    JudgingActionRequest request = JudgingActionRequest.from(target, action);

    HttpEntity<JudgingActionRequest> entity = new HttpEntity<>(request, headers);

    // URL 및 요청 데이터 로깅
    LOGGER.info("Request URL: {}", url);
    LOGGER.info("Request Data: {}", request);

    try {
      ResponseEntity<SuccessResponse> response =
          restTemplate.postForEntity(url, entity, SuccessResponse.class);
      if (response.getStatusCode().is5xxServerError()) {
        LOGGER.info("[requestGrading] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
        throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return response;
    } catch (HttpServerErrorException e) {
      // 서버 오류에 대한 상세 정보 로깅
      LOGGER.error(
          "[requestGrading] Internal Server Error: {}, URL: {}", e.getResponseBodyAsString(), url);
      throw e;
    } catch (ResourceAccessException e) {
      // 리소스 접근 오류에 대한 상세 정보 로깅
      LOGGER.error("[requestGrading] Resource Access Error: {}, URL: {}", e.getMessage(), url);
      throw e;
    }
  }

  @LogExecutionTime
  public ResponseEntity<SuccessResponse> requestCompose(
      JudgingTarget target, JudgingAction action) {
    WorkspacePublicId publicId = loadPublicId(target);
    String url = LabExternalEndpoints.composeUrl(publicIdQuery(publicId));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    JudgingActionRequest request = JudgingActionRequest.from(target, action);
    LOGGER.info("qurl: {}", url);
    HttpEntity<JudgingActionRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<SuccessResponse> response =
        restTemplate.postForEntity(url, entity, SuccessResponse.class);
    if (response.getStatusCode().is5xxServerError()) {
      LOGGER.info("[requestCompose] 실습서버가 정상적으로 응답하지 않습니다. url: {}", url);
      throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return response;
  }

  private WorkspacePublicId loadPublicId(JudgingTarget target) {
    VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(target.playerId(), target.stageCode());

    return virtualWorkspaceRepository
        .findById(workspaceId)
        .map(workspace -> workspace.publicId())
        .orElseThrow(() ->
            new BusinessException(ErrorCode.VIRTUAL_WORKSPACE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  private UrlQuery publicIdQuery(WorkspacePublicId publicId) {
    return UrlQuery.from(Pair.of("publicId", publicId.value()));
  }
}
