package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.domain.stage.domain.StageImageType;

public interface PodFactory {
    Pod createPod(StageImageType imageType, int port, String seq);
}
