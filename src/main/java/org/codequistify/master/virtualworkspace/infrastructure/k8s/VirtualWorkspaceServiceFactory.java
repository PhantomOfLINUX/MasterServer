package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;

public interface VirtualWorkspaceServiceFactory {
    Service create(VirtualWorkspace workspace);
}

