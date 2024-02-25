package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.domain.lab.vo.Label;

public interface ServiceFactory {
    Service create(String name, int port, int targetPort, Label selector);
}
