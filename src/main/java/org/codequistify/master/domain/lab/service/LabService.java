package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.dto.PShellCreateResponse;
import org.codequistify.master.domain.lab.dto.PShellExistsResponse;
import org.codequistify.master.domain.lab.domain.VirtualWorkspace;
import org.codequistify.master.domain.lab.domain.VirtualWorkspaceEntity;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.codequistify.master.domain.lab.vo.LabServiceName;
import org.codequistify.master.domain.lab.vo.LabUserUid;
import org.codequistify.master.domain.lab.vo.LabResourceId;
import org.codequistify.master.domain.lab.vo.StageCode;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final KubernetesResourceManager kubernetesResourceManager;
    private final VirtualWorkspaceRouteService virtualWorkspaceRouteService;
    private final StageSearchService stageSearchService;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);
    private final static int THRESHOLD = 20;
    private final static int SLEEP_PERIOD = 5000;

    @LogExecutionTime
    public PShellCreateResponse recreateStageOnKubernetes(String labHost, Long stageId, Player player) {
        VirtualWorkspace workspace = resolveWorkspace(stageId, player);
        LabResourceId labResourceId = workspace.resourceId();

        deleteSyncStageOnKubernetes(labResourceId);
        createStageOnKubernetes(labResourceId);

        LOGGER.info("[createStageOnKubernetes] stage: {}", labResourceId.stage().getId());

        waitForPodReadiness(labResourceId);
        return workspace.toAccessResponse(labHost);
    }

    @LogExecutionTime
    public PShellCreateResponse getPShellAccessUrl(String labHost, Long stageId, Player player) {
        VirtualWorkspace workspace = resolveWorkspace(stageId, player);
        return workspace.toAccessResponse(labHost);
    }

    @LogExecutionTime
    public PShellExistsResponse checkPShellExistence(Long stageId, Player player) {
        VirtualWorkspace workspace = resolveWorkspace(stageId, player);
        LabResourceId labResourceId = workspace.resourceId();

        boolean podExists = kubernetesResourceManager.existsPod(labResourceId);
        boolean serviceExists = kubernetesResourceManager.existsService(labResourceId);

        LOGGER.info("[existsStageOnKubernetes] pod: {}, svc: {}", podExists, serviceExists);

        return new PShellExistsResponse(
                labResourceId.uid().value(),
                stageId,
                workspace.stage().getStageImage().name(),
                podExists && serviceExists
        );
    }

    private VirtualWorkspace resolveWorkspace(Long stageId, Player player) {
        Stage stage = stageSearchService.getStageById(stageId);
        StageCode stageCode = StageCode.from(stage);
        LabUserUid uid = LabUserUid.from(player);
        LabRouteId routeId = LabRouteId.of(player.id(), stageCode);
        LabServiceName issuedName = LabServiceName.issue(stageCode, uid);

        VirtualWorkspaceEntity entity = virtualWorkspaceRouteService.getOrCreate(routeId, issuedName);

        return VirtualWorkspace.of(
                routeId,
                uid,
                entity.serviceNameVo(),
                entity.getServiceDns(),
                stage
        );
    }

    private void createStageOnKubernetes(LabResourceId labResourceId) {
        kubernetesResourceManager.createServiceOnKubernetes(labResourceId);
        kubernetesResourceManager.createPodOnKubernetes(labResourceId);
    }

    private void deleteAsyncStageOnKubernetes(LabResourceId labResourceId) {
        kubernetesResourceManager.deleteAsyncPod(labResourceId);
        kubernetesResourceManager.deleteAsyncService(labResourceId);
    }

    private void deleteSyncStageOnKubernetes(LabResourceId labResourceId) {
        deleteAsyncStageOnKubernetes(labResourceId);

        boolean podDeleted = false;
        boolean serviceDeleted = false;
        int retryCount = 0;

        while (!podDeleted || !serviceDeleted) {
            if (!podDeleted && !kubernetesResourceManager.existsPod(labResourceId)) {
                podDeleted = true;
                LOGGER.info("[deleteSyncStageOnKubernetes] Pod 삭제 확인 {}번 시도", retryCount);
            }
            if (!serviceDeleted && !kubernetesResourceManager.existsService(labResourceId)) {
                serviceDeleted = true;
                LOGGER.info("[deleteSyncStageOnKubernetes] Service 삭제 확인 {}번 시도", retryCount);
            }
            if (retryCount > THRESHOLD) {
                LOGGER.error("[deleteSyncStageOnKubernetes] {}",ErrorCode.PSHELL_CREATE_FAILED.getMessage());
                throw new BusinessException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            try {
                Thread.sleep(SLEEP_PERIOD);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("[deleteSyncStageOnKubernetes] 인터럽트 오류 발생");
                throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void waitForPodReadiness(LabResourceId labResourceId) {
        int retryCount = 0;
        while (true) {
            Pod pod = kubernetesResourceManager.getPod(labResourceId);
            if (pod != null && pod.getStatus() != null && pod.getStatus().getConditions() != null) {
                for (PodCondition condition : pod.getStatus().getConditions()) {
                    if ("Ready".equals(condition.getType()) && "True".equals(condition.getStatus())) {
                        LOGGER.info("[waitForPodReadiness] 네트워크 구성완료, pod: {}, time: {}ms", pod.getMetadata().getName(), retryCount * 2000);
                        return;
                    }
                }
            }

            if (retryCount > THRESHOLD) {
                LOGGER.error("[checkPodReady] {}",ErrorCode.PSHELL_CREATE_FAILED.getMessage());
                throw new BusinessException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            try {
                Thread.sleep(2000L);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("[checkPodReady] 인터럽트 오류 발생");
                throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

}
