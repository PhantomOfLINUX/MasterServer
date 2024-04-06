package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.service.impl.StageServiceImpl;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final StageServiceImpl stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);

    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

    private final KubernetesClient kubernetesClient;

    @LogMonitoring
    public void createStageOnKubernetes(Player player, Stage stage){
        String uid = player.getUid().toLowerCase();

        this.createServiceOnKubernetes(stage, uid);
        this.createPodOnKubernetes(stage, uid);

        LOGGER.info("[createStageOnKubernetes] stage: {}", stage.getId());

    }

    @LogMonitoring
    public void deleteStageOnKubernetes(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();

        this.deletePodIfExists(stage, uid);
        this.deleteServiceIfExists(stage, uid);
    }

    @LogExecutionTime
    private Service createServiceOnKubernetes(Stage stage, String uid) {
        Service service = serviceFactory.create(stage, 8080, uid);

        service = kubernetesClient.services()
                .inNamespace("default")
                .resource(service)
                .create();

        LOGGER.info("[createServiceOnKubernetes] service: {}", service.getMetadata());
        return service;
    }

    @LogExecutionTime
    private Pod createPodOnKubernetes(Stage stage, String uid) {
        Pod pod = podFactory.create(stage, 8080, uid);

        pod = kubernetesClient.pods()
                .inNamespace("default")
                .resource(pod)
                .create();

        LOGGER.info("[createPodOnKubernetes] pod: {}", pod.getMetadata());
        return pod;
    }

    @LogExecutionTime
    private void deletePodIfExists(Stage stage, String uid) {
        String podName = stage.getStageImage().name().toLowerCase() + "-pod-" + uid;

        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .delete();

        LOGGER.info("[deletePodIfExists] result {}", result);
    }

    @LogExecutionTime
    private void deleteServiceIfExists(Stage stage, String uid) {
        String svcName = stage.getStageImage().name().toLowerCase() + "-svc-" + uid;

        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .delete();

        LOGGER.info("[deleteServiceIfExists] result {}", result);
    }

    public boolean waitForResourceDeletion(Player player, Stage stage) {
        String uid = player.getUid().toLowerCase();
        int threshold = 20;
        int retryCount = 0;

        while (retryCount < threshold) {
            if (!resourceExists(stage, uid)) {
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


    @LogExecutionTime
    private boolean resourceExists(Stage stage, String uid) {
        String podName = stage.getStageImage().name().toLowerCase() + "-pod-" + uid;
        String svcName = stage.getStageImage().name().toLowerCase() + "-svc-" + uid;

        // 포드 존재 여부 확인
        boolean podExists = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .get() != null;

        // 서비스 존재 여부 확인
        boolean serviceExists = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .get() != null;

        // 포드 또는 서비스가 존재하면 true 반환
        return podExists || serviceExists;
    }

}
