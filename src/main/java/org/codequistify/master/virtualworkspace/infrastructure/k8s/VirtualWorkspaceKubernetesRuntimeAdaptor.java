package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.virtualworkspace.application.port.VirtualWorkspaceClient;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VirtualWorkspaceKubernetesRuntimeAdaptor implements VirtualWorkspaceClient {
  private final VirtualWorkspaceKubernetesManager kubernetesManager;

  @Override
  public void provision(VirtualWorkspace workspace) {
    kubernetesManager.createService(workspace);
    kubernetesManager.createPod(workspace);
  }

  @Override
  public void deprovision(WorkspacePublicId publicId) {
    kubernetesManager.deleteServiceAsync(publicId);
    kubernetesManager.deletePodAsync(publicId);
  }

  @Override
  public void deprovisionSync(WorkspacePublicId publicId) {
    kubernetesManager.deleteServiceSync(publicId);
    kubernetesManager.deletePodSync(publicId);
  }

  @Override
  public boolean exists(WorkspacePublicId publicId) {
    return kubernetesManager.existsService(publicId) && kubernetesManager.existsPod(publicId);
  }

  @Override
  public void waitUntilReady(WorkspacePublicId publicId) {
    kubernetesManager.waitForPodReadiness(publicId);
  }
}
