package org.codequistify.master.application.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final KubernetesResourceManager kubernetesResourceManager;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);
    private final static int THRESHOLD = 20;
    private final static int SLEEP_PERIOD = 5000;

    @LogExecutionTime
    public void createStageOnKubernetes(Player player, StageEntity stageEntity) {
        kubernetesResourceManager.createServiceOnKubernetes(stageEntity, uid);
        kubernetesResourceManager.createPodOnKubernetes(stageEntity, uid);

        LOGGER.info("[createStageOnKubernetes] stageEntity: {}", stageEntity.getId());

    }

    @LogExecutionTime
    public void deleteAsyncStageOnKubernetes(Player player, StageEntity stageEntity) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stageEntity, uid);
        kubernetesResourceManager.deleteAsyncService(stageEntity, uid);
    }

    @LogExecutionTime
    public void deleteSyncStageOnKubernetes(Player player, StageEntity stageEntity) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stageEntity, uid);
        kubernetesResourceManager.deleteAsyncService(stageEntity, uid);

        boolean podDeleted = false;
        boolean serviceDeleted = false;
        int retryCount = 0;

        while (!podDeleted || !serviceDeleted) {
            if (!podDeleted && !kubernetesResourceManager.existsPod(stageEntity, uid)) {
                podDeleted = true;
                LOGGER.info("[deleteSyncStageOnKubernetes] Pod 삭제 확인 {}번 시도", retryCount);
            }
            if (!serviceDeleted && !kubernetesResourceManager.existsService(stageEntity, uid)) {
                serviceDeleted = true;
                LOGGER.info("[deleteSyncStageOnKubernetes] Service 삭제 확인 {}번 시도", retryCount);
            }
            if (retryCount > THRESHOLD) {
                LOGGER.error("[deleteSyncStageOnKubernetes] {}",ErrorCode.PSHELL_CREATE_FAILED.getMessage());
                throw new ApplicationException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            try {
                Thread.sleep(SLEEP_PERIOD);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("[deleteSyncStageOnKubernetes] 인터럽트 오류 발생");
                throw new ApplicationException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @LogExecutionTime
    public boolean existsStageOnKubernetes(Player player, StageEntity stageEntity) {
        boolean podExists = kubernetesResourceManager.existsPod(stageEntity, player.getUid());
        boolean serviceExists = kubernetesResourceManager.existsService(stageEntity, player.getUid());

        LOGGER.info("[existsStageOnKubernetes] pod: {}, svc: {}", podExists, serviceExists);
        return podExists && serviceExists;
    }

    @LogExecutionTime
    public void waitForPodReadiness(Player player, StageEntity stageEntity) {
        String uid = player.getUid().toLowerCase();
        int retryCount = 0;
        while (true) {
            Pod pod = kubernetesResourceManager.getPod(stageEntity, uid);
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
                throw new ApplicationException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            try {
                Thread.sleep(2000L);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("[checkPodReady] 인터럽트 오류 발생");
                throw new ApplicationException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

}
