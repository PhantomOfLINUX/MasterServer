package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.lab.vo.Label;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.codequistify.master.domain.stage.service.impl.StageServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LabService {
    private final StageServiceImpl stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(LabService.class);
    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;
    private final KubernetesClient kubernetesClient;

    @Transactional
    public Integer createStageOnKubernetes(Long stageId){
        Stage stage = stageService.findStageById(stageId);

        String uuid = UUID.randomUUID().toString();
        Label selector = new Label("uuid", uuid);

        Service service = createServiceOnKubernetes(stage, uuid, selector);
        Pod pod = createPodOnKubernetes(stage, uuid, selector);

        Integer nodePort = service.getSpec().getPorts().get(0).getNodePort();

        return nodePort;
    }

    private Service createServiceOnKubernetes(Stage stage, String uuid, Label selector) {
        String serviceName = generateServiceName(stage.getStageImageType(), uuid);
        Service service = serviceFactory.create(serviceName, 8080, 8080, selector);

        service = kubernetesClient.services()
                .inNamespace("pol-lab")
                .resource(service)
                .create();
        LOGGER.info("[] service: {}", service);
        return service;
    }

    private Pod createPodOnKubernetes(Stage stage, String uuid, Label selector) {
        String podName = generatePodName(stage.getStageImageType(), uuid);
        Pod pod = podFactory.create(podName, stage.getStageImageType(), 8080, selector);

        pod = kubernetesClient.pods()
                .inNamespace("pol-lab")
                .resource(pod)
                .create();
        LOGGER.info("[] pod: {}", pod);
        return pod;
    }

    private String generateServiceName(StageImageType imageType, String uuid) {
        return imageType.name().toLowerCase() + "-svc-" + uuid;
    }

    private String generatePodName(StageImageType imageType, String uuid) {
        return imageType.name().toLowerCase() + "-pod-" + uuid;
    }
}
