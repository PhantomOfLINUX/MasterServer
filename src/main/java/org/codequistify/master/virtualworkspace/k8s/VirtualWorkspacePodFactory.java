package org.codequistify.master.domain.lab.virtualworkspace.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.domain.lab.virtualworkspace.domain.VirtualWorkspace;

public interface VirtualWorkspacePodFactory {
    Pod create(VirtualWorkspace workspace);
}

