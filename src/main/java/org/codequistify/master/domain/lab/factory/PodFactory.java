package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.domain.lab.vo.Label;
import org.codequistify.master.domain.stage.domain.StageImageType;

public interface PodFactory {
    Pod create(String name, StageImageType imageType, int port, Label selector);
}
