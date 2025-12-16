package org.codequistify.master.domain.lab.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.config.LabInfrastructureDefaults;
import org.codequistify.master.domain.lab.domain.VirtualWorkspaceEntity;
import org.codequistify.master.domain.lab.repository.VirtualWorkspaceRepository;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.codequistify.master.domain.lab.vo.LabServiceName;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VirtualWorkspaceRouteService {
    private final VirtualWorkspaceRepository virtualWorkspaceRepository;

    public VirtualWorkspaceEntity getOrCreate(LabRouteId routeId, LabServiceName serviceName) {
        return virtualWorkspaceRepository
                .findByRouteId(routeId)
                .orElseGet(() -> issueRoute(routeId, serviceName));
    }

    private VirtualWorkspaceEntity issueRoute(LabRouteId routeId, LabServiceName serviceName) {
        VirtualWorkspaceEntity entity = VirtualWorkspaceEntity.create(
                routeId,
                serviceName,
                LabInfrastructureDefaults.LAB_NAMESPACE
        );
        return virtualWorkspaceRepository.save(entity);
    }
}
