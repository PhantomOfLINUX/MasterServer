package org.codequistify.master.application.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.stage.util.RetryExecutor;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.Stage;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codequistify.master.application.stage.util.RetryExecutor.retryUntil;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private static final int SLEEP_PERIOD = 5000;
    private static final int THRESHOLD = 20;
    private final Logger logger = LoggerFactory.getLogger(LabService.class);
    private final KubernetesResourceManager kubernetesResourceManager;

    @LogExecutionTime
    public void createStageOnKubernetes(Player player, Stage stage) {
        PolId uid = player.getUid();
        kubernetesResourceManager.createServiceOnKubernetes(stage, uid);
        kubernetesResourceManager.createPodOnKubernetes(stage, uid);
        logger.info("[createStageOnKubernetes] stage: {}", stage.getId());
    }

    @LogExecutionTime
    public void deleteAsyncStageOnKubernetes(Player player, Stage stage) {
        PolId uid = player.getUid();
        kubernetesResourceManager.deleteAsyncPod(stage, uid);
        kubernetesResourceManager.deleteAsyncService(stage, uid);
    }

    @LogExecutionTime
    public void deleteSyncStageOnKubernetes(Player player, Stage stage) {
        PolId uid = player.getUid();

        retryUntil(retryCount -> {
            boolean podExists = kubernetesResourceManager.existsPod(stage, uid);
            boolean serviceExists = kubernetesResourceManager.existsService(stage, uid);

            boolean podDeleted = !podExists;
            boolean serviceDeleted = !serviceExists;

            if (podDeleted) {
                logger.info("[deleteSyncStageOnKubernetes] Pod 삭제 확인 {}번 시도", retryCount);
            }
            if (serviceDeleted) {
                logger.info("[deleteSyncStageOnKubernetes] Service 삭제 확인 {}번 시도", retryCount);
            }

            return podDeleted && serviceDeleted;
        }, THRESHOLD, SLEEP_PERIOD, "deleteSyncStageOnKubernetes");
    }

    @LogExecutionTime
    public boolean existsStageOnKubernetes(Player player, Stage stage) {
        PolId uid = player.getUid();
        boolean podExists = kubernetesResourceManager.existsPod(stage, uid);
        boolean serviceExists = kubernetesResourceManager.existsService(stage, uid);

        logger.info("[existsStageOnKubernetes] pod: {}, svc: {}", podExists, serviceExists);
        return podExists && serviceExists;
    }

    @LogExecutionTime
    public void waitForPodReadiness(Player player, Stage stage) {
        PolId uid = player.getUid();

        RetryExecutor.retryUntil(retryCount -> {
            Pod pod = kubernetesResourceManager.getPod(stage, uid);

            boolean isReady = pod != null &&
                    pod.getStatus() != null &&
                    pod.getStatus().getConditions() != null &&
                    pod.getStatus().getConditions().stream()
                       .anyMatch(cond -> "Ready".equals(cond.getType()) && "True".equals(cond.getStatus()));

            if (isReady) {
                String podName = pod.getMetadata().getName();
                long waitTime = retryCount * 2000L;
                logger.info("[waitForPodReadiness] 네트워크 구성완료, pod: {}, time: {}ms", podName, waitTime);
            }

            return isReady;
        }, THRESHOLD, 2000L, "checkPodReady");
    }
}
