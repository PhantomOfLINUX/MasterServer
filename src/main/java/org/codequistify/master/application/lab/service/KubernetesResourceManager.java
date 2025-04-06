package org.codequistify.master.application.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.lab.factory.PodFactory;
import org.codequistify.master.core.domain.lab.factory.ServiceFactory;
import org.codequistify.master.core.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class KubernetesResourceManager {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesResourceManager.class);
    private final KubernetesClient kubernetesClient;
    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

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

    public List<Pod> getTimeOutPods() {
        PodList podList = kubernetesClient.pods().inNamespace("default").list();

        return podList.getItems().stream()
                      .filter(this::isPhaseFailed)
                      .filter(this::isReasonDeadlineExceeded)
                      .filter(this::hasErrorStatus)
                      .toList();
    }

    private boolean isPhaseFailed(Pod pod) {
        return "Failed".equals(pod.getStatus().getPhase());
    }

    private boolean isReasonDeadlineExceeded(Pod pod) {
        return "DeadlineExceeded".equals(pod.getStatus().getReason());
    }

    private boolean hasErrorStatus(Pod pod) {
        return pod.getStatus().getContainerStatuses().stream()
                  .anyMatch(status -> status.getState().getTerminated() != null &&
                          "Error".equals(status.getState().getTerminated().getReason()));
    }

}
