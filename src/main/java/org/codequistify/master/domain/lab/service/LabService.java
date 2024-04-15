package org.codequistify.master.domain.lab.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
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
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);
    private final static int THRESHOLD = 20;
    private final static int SLEEP_PERIOD = 5000;

    @LogExecutionTime
    public void createStageOnKubernetes(Player player, Stage stage){
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.createServiceOnKubernetes(stage, uid);
        kubernetesResourceManager.createPodOnKubernetes(stage, uid);

        LOGGER.info("[createStageOnKubernetes] stage: {}", stage.getId());

    }

    @LogExecutionTime
    public void deleteAsyncStageOnKubernetes(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);
    }

    @LogExecutionTime
    public void deleteSyncStageOnKubernetes(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);

        boolean podDeleted = false;
        boolean serviceDeleted = false;
        int retryCount = 0;

        while (!podDeleted || !serviceDeleted) {
            if (!podDeleted && !kubernetesResourceManager.existsPod(stage, uid)) {
                podDeleted = true;
                LOGGER.info("[deleteSyncStageOnKubernetes] Pod 삭제 확인 {}번 시도", retryCount);
            }
            if (!serviceDeleted && !kubernetesResourceManager.existsService(stage, uid)) {
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

    public boolean existsStageOnKubernetes(Player player, Stage stage) {
        boolean podExists = kubernetesResourceManager.existsPod(stage, player.getUid());
        boolean serviceExists = kubernetesResourceManager.existsService(stage, player.getUid());

        LOGGER.info("[existsStageOnKubernetes] pod: {}, svc: {}", podExists, serviceExists);
        return podExists && serviceExists;
    }

}
