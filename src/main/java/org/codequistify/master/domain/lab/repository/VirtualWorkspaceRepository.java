package org.codequistify.master.domain.lab.repository;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.domain.LabK8sRouteKey;
import org.codequistify.master.domain.lab.domain.VirtualWorkspaceEntity;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VirtualWorkspaceRepository {
    private final VirtualWorkspaceJpaRepository jpaRepository;

    public Optional<VirtualWorkspaceEntity> findByRouteId(LabRouteId routeId) {
        LabK8sRouteKey key = new LabK8sRouteKey(routeId.playerId().value(), routeId.stageCode().value());
        return jpaRepository.findById(key);
    }

    public VirtualWorkspaceEntity save(VirtualWorkspaceEntity entity) {
        return jpaRepository.save(entity);
    }
}
