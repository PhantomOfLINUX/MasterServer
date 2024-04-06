package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.service.impl.StageServiceImpl;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final StageServiceImpl stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);

    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

    private final KubernetesClient kubernetesClient;

    @LogExecutionTime
    public void createStageOnKubernetes(Player player, Stage stage){
        String uid = player.getUid().toLowerCase();

        this.createServiceOnKubernetes(stage, uid);
        this.createPodOnKubernetes(stage, uid);

        LOGGER.info("[createStageOnKubernetes] stage: {}", stage.getId());

    }

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

        LOGGER.info("[createServiceOnKubernetes] service: {}", service.getSpec());
        return service;
    }

    @LogExecutionTime
    private Pod createPodOnKubernetes(Stage stage, String uid) {
        Pod pod = podFactory.create(stage, 8080, uid);

        pod = kubernetesClient.pods()
                .inNamespace("default")
                .resource(pod)
                .create();

        LOGGER.info("[createPodOnKubernetes] pod: {}", pod.getSpec());
        return pod;
    }

    @LogExecutionTime
    private void deletePodIfExists(Stage stage, String uid) {
        String podName = stage.getStageImage().name().toLowerCase() + "-pod-" + uid;
        LOGGER.info(podName);

        kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .delete();
    }

    @LogExecutionTime
    private void deleteServiceIfExists(Stage stage, String uid) {
        String svcName = stage.getStageImage().name().toLowerCase() + "-svc-" + uid;
        LOGGER.info(svcName);

        kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .delete();
    }
}
