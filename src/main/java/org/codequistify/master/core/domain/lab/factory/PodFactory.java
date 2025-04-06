package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;

public interface PodFactory {
    Pod create(StageEntity stageEntity, int port, PolId uid);
}
