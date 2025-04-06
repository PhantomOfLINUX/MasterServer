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
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class KubernetesResourceManager {

    private static final String NAMESPACE = "default";
    private final KubernetesClient kubernetesClient;
    private final Logger logger = LoggerFactory.getLogger(KubernetesResourceManager.class);
    private final PodFactory podFactory;
    private final ServiceFactory serviceFactory;

    public Service createServiceOnKubernetes(Stage stage, PolId uid) {
        return Optional.of(serviceFactory.create(stage, 8080, uid))
                       .map(service -> kubernetesClient.services()
                                                       .inNamespace(NAMESPACE)
                                                       .resource(service)
                                                       .create())
                       .map(service -> {
                           logger.debug("[createServiceOnKubernetes] service: {}", service.getMetadata().getName());
                           return service;
                       })
                       .orElseThrow();
    }

    public Pod createPodOnKubernetes(Stage stage, PolId uid) {
        return Optional.of(podFactory.create(stage, 8080, uid))
                       .map(pod -> kubernetesClient.pods()
                                                   .inNamespace(NAMESPACE)
                                                   .resource(pod)
                                                   .create())
                       .map(pod -> {
                           logger.debug("[createPodOnKubernetes] pod: {}", pod.getMetadata().getName());
                           return pod;
                       })
                       .orElseThrow();
    }

    public void deleteAsyncService(Stage stage, PolId uid) {
        deleteAsyncService(KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid));
    }

    public void deleteAsyncService(String svcName) {
        logDeletion("Service", svcName,
                    () -> kubernetesClient.services()
                                          .inNamespace(NAMESPACE)
                                          .withName(svcName)
                                          .delete());
    }

    public void deleteAsyncPod(Stage stage, PolId uid) {
        deleteAsyncPod(KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid));
    }

    public void deleteAsyncPod(String podName) {
        logDeletion("Pod", podName,
                    () -> kubernetesClient.pods()
                                          .inNamespace(NAMESPACE)
                                          .withName(podName)
                                          .delete());
    }

    public Service getService(Stage stage, PolId uid) {
        String name = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);
        return Optional.ofNullable(kubernetesClient.services()
                                                   .inNamespace(NAMESPACE)
                                                   .withName(name)
                                                   .get())
                       .map(service -> {
                           logger.debug("[getService] name: {}", name);
                           return service;
                       })
                       .orElseThrow();
    }

    public Pod getPod(Stage stage, PolId uid) {
        String name = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);
        return Optional.ofNullable(kubernetesClient.pods()
                                                   .inNamespace(NAMESPACE)
                                                   .withName(name)
                                                   .get())
                       .map(pod -> {
                           logger.debug("[getPod] name: {}", name);
                           return pod;
                       })
                       .orElseThrow();
    }

    public boolean existsService(Stage stage, PolId uid) {
        String name = KubernetesResourceNaming.getServiceName(stage.getStageImage().name(), uid);
        return kubernetesClient.services()
                               .inNamespace(NAMESPACE)
                               .withName(name)
                               .get() != null;
    }

    public boolean existsPod(Stage stage, PolId uid) {
        String name = KubernetesResourceNaming.getPodName(stage.getStageImage().name(), uid);
        return kubernetesClient.pods()
                               .inNamespace(NAMESPACE)
                               .withName(name)
                               .get() != null;
    }

    public List<Pod> getTimeOutPods() {
        return Optional.ofNullable(kubernetesClient.pods().inNamespace(NAMESPACE).list())
                       .map(PodList::getItems)
                       .stream()
                       .flatMap(List::stream)
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
        return Optional.ofNullable(pod.getStatus().getContainerStatuses())
                       .stream()
                       .flatMap(List::stream)
                       .anyMatch(status ->
                                         Optional.ofNullable(status.getState().getTerminated())
                                                 .map(t -> "Error".equals(t.getReason()))
                                                 .orElse(false));
    }

    private void logDeletion(String kind, String name, Supplier<List<StatusDetails>> deletion) {
        List<StatusDetails> result = deletion.get();
        logger.debug("[delete{}] name={}, result={}", kind, name, result);
    }
}
