package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceDefaults;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class VirtualWorkspaceKubernetesManager {
  private final Logger logger = LoggerFactory.getLogger(VirtualWorkspaceKubernetesManager.class);

  private static final int THRESHOLD = 20;
  private static final int DELETE_SLEEP_PERIOD_MS = 5000;
  private static final int READINESS_SLEEP_PERIOD_MS = 2000;

  private final KubernetesClient kubernetesClient;
  private final VirtualWorkspacePodFactory podFactory;
  private final VirtualWorkspaceServiceFactory serviceFactory;

  public void createService(VirtualWorkspace workspace) {
    Service service = serviceFactory.create(workspace);

    kubernetesClient
        .services()
        .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
        .resource(service)
        .create();

    logger.debug("[createService] service: {}", service.getMetadata().getName());
  }

  public void createPod(VirtualWorkspace workspace) {
    Pod pod = podFactory.create(workspace);

    kubernetesClient
        .pods()
        .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
        .resource(pod)
        .create();

    logger.debug("[createPod] pod: {}", pod.getMetadata().getName());
  }

  public void deleteService(WorkspacePublicId publicId) {
    List<StatusDetails> result = kubernetesClient
        .services()
        .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
        .withName(publicId.value())
        .delete();

    logger.debug("[deleteService] result {}", result);
  }

  public void deletePod(WorkspacePublicId publicId) {
    List<StatusDetails> result = kubernetesClient
        .pods()
        .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
        .withName(publicId.value())
        .delete();

    logger.debug("[deletePod] result {}", result);
  }

  public boolean existsService(WorkspacePublicId publicId) {
    return kubernetesClient
            .services()
            .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
            .withName(publicId.value())
            .get()
        != null;
  }

  public boolean existsPod(WorkspacePublicId publicId) {
    return kubernetesClient
            .pods()
            .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
            .withName(publicId.value())
            .get()
        != null;
  }

  public Pod getPod(WorkspacePublicId publicId) {
    return kubernetesClient
        .pods()
        .inNamespace(VirtualWorkspaceDefaults.NAMESPACE)
        .withName(publicId.value())
        .get();
  }

  public void deleteSync(WorkspacePublicId publicId) {
    deleteService(publicId);
    deletePod(publicId);

    boolean podDeleted = false;
    boolean serviceDeleted = false;
    int retryCount = 0;

    while (!podDeleted || !serviceDeleted) {
      if (!serviceDeleted && !existsService(publicId)) {
        serviceDeleted = true;
        logger.info("[deleteSync] Service 삭제 확인 {}번 시도", retryCount);
      }
      if (!podDeleted && !existsPod(publicId)) {
        podDeleted = true;
        logger.info("[deleteSync] Pod 삭제 확인 {}번 시도", retryCount);
      }
      if (retryCount > THRESHOLD) {
        throw new IllegalStateException("VirtualWorkspace delete timeout");
      }
      try {
        Thread.sleep(DELETE_SLEEP_PERIOD_MS);
        retryCount++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("VirtualWorkspace delete interrupted", e);
      }
    }
  }

  public void waitForPodReadiness(WorkspacePublicId publicId) {
    int retryCount = 0;
    while (true) {
      Pod pod = getPod(publicId);
      if (podReady(pod)) {
        logger.info(
            "[waitForPodReadiness] pod: {}, time: {}ms",
            publicId.value(),
            retryCount * READINESS_SLEEP_PERIOD_MS);
        return;
      }

      if (retryCount > THRESHOLD) {
        throw new IllegalStateException("VirtualWorkspace readiness timeout");
      }
      try {
        Thread.sleep(READINESS_SLEEP_PERIOD_MS);
        retryCount++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("VirtualWorkspace readiness interrupted", e);
      }
    }
  }

  private boolean podReady(Pod pod) {
    if (pod == null || pod.getStatus() == null || pod.getStatus().getConditions() == null) {
      return false;
    }

    for (PodCondition condition : pod.getStatus().getConditions()) {
      if ("Ready".equals(condition.getType()) && "True".equals(condition.getStatus())) {
        return true;
      }
    }
    return false;
  }
}
