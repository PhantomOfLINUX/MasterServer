package org.codequistify.master.virtualworkspace.infrastructure.persistence.repository;

import java.util.Optional;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.entity.VirtualWorkspaceTableEntity;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.entity.VirtualWorkspaceTableKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualWorkspaceJpaRepository
    extends JpaRepository<VirtualWorkspaceTableEntity, VirtualWorkspaceTableKey> {
  Optional<VirtualWorkspaceTableEntity> findByPublicId(String publicId);
}
