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

    @LogExecutionTime
    public void createStageOnKubernetes(Player player, Stage stage){
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.createServiceOnKubernetes(stage, uid);
        kubernetesResourceManager.createPodOnKubernetes(stage, uid);

        LOGGER.info("[createStageOnKubernetes] stage: {}", stage.getId());

    }

    @LogExecutionTime
    public void deleteStageOnKubernetes(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);
    }

    @LogExecutionTime
    public void deleteSyncStageOnKubernetes(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();

        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);

    }

    @LogExecutionTime
    public boolean existsStageOnKubernetes(Player player, Stage stage) {
        boolean podExists = kubernetesResourceManager.getPod(stage, player.getUid()) != null;
        boolean serviceExists = kubernetesResourceManager.getService(stage, player.getUid()) != null;

        return podExists || serviceExists;
    }

    public boolean waitForResourceDeletion(Player player, Stage stage) {
        int threshold = 20;
        int retryCount = 0;

        while (retryCount < threshold) {
            if (!existsStageOnKubernetes(player, stage)) {
                LOGGER.info("[waitForResourceDeletion] {}번 시도", retryCount);
                return true;  // 리소스가 더 이상 존재하지 않으면 삭제 성공
            }
            try {
                Thread.sleep(5000);  // 5초 간격으로 다시 확인
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        LOGGER.info("[waitForResourceDeletion] 최대 시도 실패");
        return false;  // 최대 시도 횟수 도달 시 삭제 실패
    }


}
