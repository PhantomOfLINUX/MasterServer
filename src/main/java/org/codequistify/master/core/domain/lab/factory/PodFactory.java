package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.domain.Stage;

public interface PodFactory {
    Pod create(Stage stage, int port, PolId uid);
}
