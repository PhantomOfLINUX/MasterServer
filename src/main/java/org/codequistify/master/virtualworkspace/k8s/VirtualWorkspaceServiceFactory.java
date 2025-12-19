package org.codequistify.master.domain.lab.virtualworkspace.k8s;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.domain.lab.virtualworkspace.domain.VirtualWorkspace;

public interface VirtualWorkspaceServiceFactory {
    Service create(VirtualWorkspace workspace);
}

