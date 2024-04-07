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

        // 삭제 요청을 먼저 같이 보내야 pod가 제거되는 동안 svc도 제거됨
        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);

        int retryCount = 0;
        while (retryCount < THRESHOLD) {
            if (!kubernetesResourceManager.existPod(stage, uid)) { // pod 삭제가 svc 보다 반드시 오래걸린다는 가정
                LOGGER.info("[deleteSyncStageOnKubernetes] 삭제 확인 {}번 시도", retryCount);
                return;
            }
            try {
                Thread.sleep(SLEEP_PERIOD);
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @LogExecutionTime
    public boolean existsStageOnKubernetes(Player player, Stage stage) {
        boolean podExists = kubernetesResourceManager.getPod(stage, player.getUid()) != null;
        boolean serviceExists = kubernetesResourceManager.getService(stage, player.getUid()) != null;

        return podExists && serviceExists;
    }

}
