package org.codequistify.master.domain.lab.virtualworkspace.repository;

import org.codequistify.master.domain.lab.virtualworkspace.persistence.VirtualWorkspaceTableEntity;
import org.codequistify.master.domain.lab.virtualworkspace.persistence.VirtualWorkspaceTableKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualWorkspaceJpaRepository extends JpaRepository<VirtualWorkspaceTableEntity, VirtualWorkspaceTableKey> {
    Optional<VirtualWorkspaceTableEntity> findByPublicId(String publicId);
}

