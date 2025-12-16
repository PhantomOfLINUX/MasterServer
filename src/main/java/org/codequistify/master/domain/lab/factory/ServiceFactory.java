package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Service;
import org.codequistify.master.domain.lab.vo.LabResourceId;

public interface ServiceFactory {
    Service create(LabResourceId resourceId, int port);
}
