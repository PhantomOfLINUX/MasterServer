package org.codequistify.master.virtualworkspace.infrastructure.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;

public interface VirtualWorkspacePodFactory {
    Pod create(VirtualWorkspace workspace);
}

