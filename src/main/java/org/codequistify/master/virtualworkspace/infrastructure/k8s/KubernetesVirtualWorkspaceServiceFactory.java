package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceRouting;
import org.codequistify.master.global.data.Labels;
import org.springframework.stereotype.Component;

@Component
public class KubernetesVirtualWorkspaceServiceFactory implements VirtualWorkspaceServiceFactory {
    @Override
    public Service create(VirtualWorkspace workspace) {
        VirtualWorkspaceRouting routing = workspace.routing();
        Labels labels = VirtualWorkspaceResourceLabels.standard(workspace);
        int port = workspace.specSnapshot().port();

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(routing.publicEndpoint().publicId().value())
                .withLabels(labels.toSingleValueMap())
                .endMetadata()
                .withNewSpec()
                .withType("ClusterIP")
                .addNewPort()
                .withName("http")
                .withProtocol("TCP")
                .withPort(port)
                .withTargetPort(new IntOrString(port))
                .endPort()
                .withSelector(labels.toSingleValueMap())
                .endSpec()
                .build();
    }
}

