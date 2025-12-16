package org.codequistify.master.domain.lab.repository;

import org.codequistify.master.domain.lab.domain.LabK8sRouteKey;
import org.codequistify.master.domain.lab.domain.VirtualWorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualWorkspaceJpaRepository extends JpaRepository<VirtualWorkspaceEntity, LabK8sRouteKey> {
}
