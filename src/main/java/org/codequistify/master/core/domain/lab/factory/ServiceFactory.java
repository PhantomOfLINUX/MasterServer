package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.Stage;

public interface ServiceFactory {
    Service create(Stage stage, int port, PolId uid);
}
