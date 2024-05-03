package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class KubernetesResourceManager {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesResourceManager.class);

    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

    private final KubernetesClient kubernetesClient;

    public Service createServiceOnKubernetes(Stage stage, String uid) {
        Service service = serviceFactory.create(stage, 8080, uid);

        service = kubernetesClient.services()
                .inNamespace("default")
                .resource(service)
                .create();

        LOGGER.debug("[createServiceOnKubernetes] service: {}", service.getMetadata().getName());
        return service;
    }

    public Pod createPodOnKubernetes(Stage stage, String uid) {
        Pod pod = podFactory.create(stage, 8080, uid);

        pod = kubernetesClient.pods()
                .inNamespace("default")
                .resource(pod)
                .create();

        LOGGER.debug("[createPodOnKubernetes] pod: {}", pod.getMetadata().getName());
        return pod;
    }

    public void deleteAsyncService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .delete();

        LOGGER.debug("[deleteAsyncService] result {}", result);
    }

    public void deleteAsyncService(String svcName) {
        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .delete();

        LOGGER.debug("[deleteAsyncService] result {}", result);
    }

    public void deleteAsyncPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);

        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .delete();

        LOGGER.debug("[deleteAsyncPod] result {}", result);
    }

    public void deleteAsyncPod(String podName) {
        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .delete();

        LOGGER.debug("[deleteAsyncPod] result {}", result);
    }

    public Service getService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        Service service = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .get();

        LOGGER.debug("[getService] name : {}", service.getMetadata().getName());
        return service;
    }

    public Pod getPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);

        Pod pod = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .get();

        LOGGER.debug("[getPod] name : {}", pod.getMetadata().getName());
        return pod;
    }

    public boolean existsService(Stage stage, String uid) {
        String svcName = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);

        boolean exists = kubernetesClient.services()
                .inNamespace("default")
                .withName(svcName)
                .get() != null;

        //LOGGER.info("[existsService] name: {}, exists: {}", svcName, exists);
        return exists;
    }

    public boolean existsPod(Stage stage, String uid) {
        String podName = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);

        boolean exists = kubernetesClient.pods()
                .inNamespace("default")
                .withName(podName)
                .get() != null;

        //LOGGER.info("[existsPod] name: {}, exists: {}", podName, exists);
        return exists;
    }

    public List<Pod> getErrorPods() {
            PodList podList = kubernetesClient.pods().list();
            List<Pod> errorPods = podList.getItems().stream()
                        .filter(pod -> "Error".equals(pod.getStatus().getPhase()))
                        .toList();
            return errorPods;
    }

}
