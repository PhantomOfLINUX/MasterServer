package org.codequistify.master.domain.lab.service;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KubernetesResourceCollector {
    private final KubernetesResourceManager kubernetesResourceManager;
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesResourceCollector.class);

    @Scheduled(cron = "0 0 * * * ?")
    public void resourceCollection() {
        List<String> timeoutPods = extractPodNames(kubernetesResourceManager.getTimeOutPods());
        timeoutPods.forEach(timeoutPod -> {
            kubernetesResourceManager.deleteAsyncPod(timeoutPod);
            kubernetesResourceManager.deleteAsyncService(timeoutPod);
        });

        LOGGER.info("[resourceCollection] PShell {}개 제거, List: {}", timeoutPods.size(), timeoutPods.toString());
    }

    private String getResourceName(Pod pod) {
        return pod.getMetadata().getName();
    }

    private List<String> extractPodNames(List<Pod> pods) {
        return pods.stream()
                .map(this::getResourceName)
                .collect(Collectors.toList());
    }

}
