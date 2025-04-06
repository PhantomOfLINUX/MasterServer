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
import org.codequistify.master.infrastructure.stage.entity.StageEntity;
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

    public Service createServiceOnKubernetes(StageEntity stageEntity, PolId uid) {
        return Optional.of(serviceFactory.create(stageEntity, 8080, uid))
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

    public Pod createPodOnKubernetes(StageEntity stageEntity, PolId uid) {
        return Optional.of(podFactory.create(stageEntity, 8080, uid))
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

    public void deleteAsyncService(StageEntity stageEntity, PolId uid) {
        deleteAsyncService(KubernetesResourceNaming.getServiceName(stageEntity.getStageImage().name(), uid));
    }

    public void deleteAsyncService(String svcName) {
        logDeletion("Service", svcName,
                    () -> kubernetesClient.services()
                                          .inNamespace(NAMESPACE)
                                          .withName(svcName)
                                          .delete());
    }

    public void deleteAsyncPod(StageEntity stageEntity, PolId uid) {
        deleteAsyncPod(KubernetesResourceNaming.getPodName(stageEntity.getStageImage().name(), uid));
    }

    public void deleteAsyncPod(String podName) {
        logDeletion("Pod", podName,
                    () -> kubernetesClient.pods()
                                          .inNamespace(NAMESPACE)
                                          .withName(podName)
                                          .delete());
    }

    public Service getService(StageEntity stageEntity, PolId uid) {
        String name = KubernetesResourceNaming.getServiceName(stageEntity.getStageImage().name(), uid);
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

    public Pod getPod(StageEntity stageEntity, PolId uid) {
        String name = KubernetesResourceNaming.getPodName(stageEntity.getStageImage().name(), uid);
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

    public boolean existsService(StageEntity stageEntity, PolId uid) {
        String name = KubernetesResourceNaming.getServiceName(stageEntity.getStageImage().name(), uid);
        return kubernetesClient.services()
                               .inNamespace(NAMESPACE)
                               .withName(name)
                               .get() != null;
    }

    public boolean existsPod(StageEntity stageEntity, PolId uid) {
        String name = KubernetesResourceNaming.getPodName(stageEntity.getStageImage().name(), uid);
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
