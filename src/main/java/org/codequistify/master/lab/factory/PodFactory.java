package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import org.codequistify.master.domain.stage.domain.Stage;

public interface PodFactory {
    Pod create(Stage stage, int port, String uid);
}
