package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import org.codequistify.master.global.data.Labels;
import org.codequistify.master.virtualworkspace.domain.model.StageSpecSnapshot;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceRouting;
import org.springframework.stereotype.Component;

@Component
public class KubernetesVirtualWorkspacePodFactory implements VirtualWorkspacePodFactory {
  private static final long ACTIVE_DEADLINE_SECONDS = 10_800L;
  private static final int READINESS_INITIAL_DELAY_SECONDS = 9;
  private static final int READINESS_PERIOD_SECONDS = 2;

  @Override
  public Pod create(VirtualWorkspace workspace) {
    VirtualWorkspaceRouting routing = workspace.routing();
    Labels labels = VirtualWorkspaceResourceLabels.standard(workspace);
    StageSpecSnapshot spec = workspace.specSnapshot();

    return new PodBuilder()
        .withNewMetadata()
        .withName(routing.publicEndpoint().publicId().value())
        .withLabels(labels.toSingleValueMap())
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(spec.stageCode().lowercase())
        .withImage(spec.image())
        .addNewPort()
        .withContainerPort(spec.port())
        .endPort()
        .withNewResources()
        .addToRequests("cpu", new Quantity(spec.cpu()))
        .addToRequests("memory", new Quantity(spec.memory()))
        .addToLimits("cpu", new Quantity(spec.cpu()))
        .addToLimits("memory", new Quantity(spec.memory()))
        .endResources()
        .withNewReadinessProbe()
        .withNewHttpGet()
        .withPath(spec.readinessPath())
        .withPort(new IntOrString(spec.port()))
        .endHttpGet()
        .withInitialDelaySeconds(READINESS_INITIAL_DELAY_SECONDS)
        .withPeriodSeconds(READINESS_PERIOD_SECONDS)
        .endReadinessProbe()
        .endContainer()
        .withActiveDeadlineSeconds(ACTIVE_DEADLINE_SECONDS)
        .endSpec()
        .build();
  }
}
