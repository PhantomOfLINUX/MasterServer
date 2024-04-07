package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class KubernetesResourceManager {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesResourceManager.class);

    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

    private final KubernetesClient kubernetesClient;

    @LogExecutionTime
    public Service createServiceOnKubernetes(Stage stage, String uid) {
        Service service = serviceFactory.create(stage, 8080, uid);

        service = kubernetesClient.services()
                .inNamespace("default")
                .resource(service)
                .create();

        LOGGER.info("[createServiceOnKubernetes] service: {}", service.getMetadata());
        return service;
    }

    @LogExecutionTime
    public Pod createPodOnKubernetes(Stage stage, String uid) {
        Pod pod = podFactory.create(stage, 8080, uid);

        pod = kubernetesClient.pods()
                .inNamespace("default")
                .resource(pod)
                .create();

        LOGGER.info("[createPodOnKubernetes] pod: {}", pod.getMetadata());
        return pod;
    }

    @LogExecutionTime
    public void deleteAsyncService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .delete();

        LOGGER.info("[deleteServiceIfExists] result {}", result);
    }

    @LogExecutionTime
    public void deleteAsyncPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);

        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .delete();

        LOGGER.info("[deletePodIfExists] result {}", result);
    }

    public Service getService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        Service service = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .get();

        LOGGER.info("[getServic] name : {}", service.getMetadata().getName());
        return service;
    }

    public Pod getPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        Pod pod = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .get();

        LOGGER.info("[getPod] name : {}", pod.getMetadata().getName());
        return pod;
    }

    public boolean existService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        boolean exist = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .get() != null;

        LOGGER.info("[existService] name : {}", svcName);
        return exist;
    }

    public boolean existPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        boolean exist = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .get() != null;

        LOGGER.info("[existPod] name : {}", podName);
        return exist;
    }

}
