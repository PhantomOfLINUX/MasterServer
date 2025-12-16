package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.config.LabInfrastructureDefaults;
import org.codequistify.master.domain.lab.factory.PodFactory;
import org.codequistify.master.domain.lab.factory.ServiceFactory;
import org.codequistify.master.domain.lab.vo.LabResourceId;
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

    public Service createServiceOnKubernetes(LabResourceId resourceId) {
        Service service = serviceFactory.create(resourceId, LabInfrastructureDefaults.LAB_SERVICE_PORT);

        service = kubernetesClient.services()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .resource(service)
                .create();

        LOGGER.debug("[createServiceOnKubernetes] service: {}", service.getMetadata().getName());
        return service;
    }

    public Pod createPodOnKubernetes(LabResourceId resourceId) {
        Pod pod = podFactory.create(resourceId, LabInfrastructureDefaults.LAB_SERVICE_PORT);

        pod = kubernetesClient.pods()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .resource(pod)
                .create();

        LOGGER.debug("[createPodOnKubernetes] pod: {}", pod.getMetadata().getName());
        return pod;
    }

    public void deleteAsyncService(LabResourceId resourceId) {
        String svcName = resourceId.resourceName().serviceName().value();

        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(svcName)
                .delete();

        LOGGER.debug("[deleteAsyncService] result {}", result);
    }

    public void deleteAsyncService(String svcName) {
        List<StatusDetails> result = kubernetesClient.services()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(svcName)
                .delete();

        LOGGER.debug("[deleteAsyncService] result {}", result);
    }

    public void deleteAsyncPod(LabResourceId resourceId) {
        String podName = resourceId.resourceName().podName();

        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(podName)
                .delete();

        LOGGER.debug("[deleteAsyncPod] result {}", result);
    }

    public void deleteAsyncPod(String podName) {
        List<StatusDetails> result = kubernetesClient.pods()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(podName)
                .delete();

        LOGGER.debug("[deleteAsyncPod] result {}", result);
    }

    public Service getService(LabResourceId resourceId) {
        String svcName = resourceId.resourceName().serviceName().value();

        Service service = kubernetesClient.services()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(svcName)
                .get();

        LOGGER.debug("[getService] name : {}", service.getMetadata().getName());
        return service;
    }

    public Pod getPod(LabResourceId resourceId) {
        String podName = resourceId.resourceName().podName();

        Pod pod = kubernetesClient.pods()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(podName)
                .get();

        LOGGER.debug("[getPod] name : {}", pod.getMetadata().getName());
        return pod;
    }

    public boolean existsService(LabResourceId resourceId) {
        String svcName = resourceId.resourceName().serviceName().value();

        boolean exists = kubernetesClient.services()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(svcName)
                .get() != null;

        //LOGGER.info("[existsService] name: {}, exists: {}", svcName, exists);
        return exists;
    }

    public boolean existsPod(LabResourceId resourceId) {
        String podName = resourceId.resourceName().podName();

        boolean exists = kubernetesClient.pods()
                .inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE)
                .withName(podName)
                .get() != null;

        //LOGGER.info("[existsPod] name: {}, exists: {}", podName, exists);
        return exists;
    }

    public List<Pod> getTimeOutPods() {
        PodList podList = kubernetesClient.pods().inNamespace(LabInfrastructureDefaults.LAB_NAMESPACE).list();

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
