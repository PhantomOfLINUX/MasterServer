package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.domain.stage.domain.Stage;

public interface ServiceFactory {
    Service create(Stage stage, int port, String uid);
}
