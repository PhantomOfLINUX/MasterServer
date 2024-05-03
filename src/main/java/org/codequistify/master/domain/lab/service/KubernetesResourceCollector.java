package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class KubernetesResourceCollector {
    private final KubernetesResourceManager kubernetesResourceManager;

    @Scheduled(cron = "0 0 * * * ?")
    public void resourceCollection() {
        kubernetesResourceManager.getErrorPods()
                .forEach(errorPod -> {
                    String resourceName = getResourceName(errorPod);
                    kubernetesResourceManager.deleteAsyncPod(resourceName);
                    kubernetesResourceManager.deleteAsyncService(resourceName);
                });
    }

    private String getResourceName(Pod pod) {
        return pod.getMetadata().getName();
    }

}
