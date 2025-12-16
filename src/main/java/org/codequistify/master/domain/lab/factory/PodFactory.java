package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.domain.lab.vo.LabResourceId;

public interface PodFactory {
    Pod create(LabResourceId resourceId, int port);
}
