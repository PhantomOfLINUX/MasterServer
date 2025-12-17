package org.codequistify.master.domain.lab.domain;

import org.codequistify.master.domain.lab.vo.KubernetesResourceName;
import org.codequistify.master.domain.lab.vo.LabResourceId;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.codequistify.master.domain.lab.vo.LabServiceName;
import org.codequistify.master.domain.lab.vo.LabUserUid;
import org.codequistify.master.domain.lab.vo.StageCode;
import org.codequistify.master.global.data.UrlQuery;
import org.codequistify.master.domain.stage.domain.Stage;

import java.util.Objects;

public record VirtualWorkspace(
        LabRouteId routeId,
        LabUserUid ownerUid,
        LabServiceName serviceName,
        String serviceDns,
        Stage stage
) {
    public VirtualWorkspace {
        Objects.requireNonNull(routeId, "routeId must not be null");
        Objects.requireNonNull(ownerUid, "ownerUid must not be null");
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        Objects.requireNonNull(serviceDns, "serviceDns must not be null");
        Objects.requireNonNull(stage, "stage must not be null");
    }

    public static VirtualWorkspace of(LabRouteId routeId, LabUserUid ownerUid, LabServiceName serviceName, String serviceDns, Stage stage) {
        return new VirtualWorkspace(routeId, ownerUid, serviceName, serviceDns, stage);
    }

    public KubernetesResourceName resourceName() {
        return KubernetesResourceName.of(routeId.stageCode(), ownerUid, serviceName);
    }

    public LabResourceId resourceId() {
        return LabResourceId.from(stage, ownerUid.value(), serviceName);
    }

    public StageCode stageCode() {
        return routeId.stageCode();
    }

    public UrlQuery accessQuery() {
        return resourceName().query();
    }
}
