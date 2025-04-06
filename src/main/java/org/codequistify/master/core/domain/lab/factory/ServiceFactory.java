package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;

public interface ServiceFactory {
    Service create(StageEntity stageEntity, int port, PolId uid);
}
