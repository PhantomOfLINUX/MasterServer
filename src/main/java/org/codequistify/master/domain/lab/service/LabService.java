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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final StageServiceImpl stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);
    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;
    private final KubernetesClient kubernetesClient;

    @Transactional
    public Integer createStageOnKubernetes(Player player, Long stageId){
        Stage stage = stageService.findStageById(stageId);

        Service service = createServiceOnKubernetes(stage, player.getUid());
        Pod pod = createPodOnKubernetes(stage, player.getUid());

        Integer nodePort = service.getSpec().getPorts().get(0).getNodePort();
        LOGGER.info("[createStageOnKubernetes] stage: {} , {} 포트에서 실행", stageId, nodePort);

        return nodePort;
    }

    private Service createServiceOnKubernetes(Stage stage, String uid) {
        Service service = serviceFactory.create(stage, 8080, uid);

        service = kubernetesClient.services()
                .inNamespace("lab")
                .resource(service)
                .create();

        LOGGER.info("[createServiceOnKubernetes] service: {}", service);
        return service;
    }

    private Pod createPodOnKubernetes(Stage stage, String uid) {
        Pod pod = podFactory.create(stage, 8080, uid);

        pod = kubernetesClient.pods()
                .inNamespace("lab")
                .resource(pod)
                .create();

        LOGGER.info("[createPodOnKubernetes] pod: {}", pod);
        return pod;
    }
}
