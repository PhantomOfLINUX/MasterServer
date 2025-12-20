package org.codequistify.master.virtualworkspace.infrastructure.persistence.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.virtualworkspace.application.port.VirtualWorkspaceRepository;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.entity.VirtualWorkspaceTableEntity;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.entity.VirtualWorkspaceTableKey;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VirtualWorkspacePersistenceAdaptor implements VirtualWorkspaceRepository {
  private final VirtualWorkspaceJpaRepository jpaRepository;

  @Override
  public Optional<VirtualWorkspace> findByPublicId(WorkspacePublicId publicId) {
    return jpaRepository
        .findByPublicId(publicId.value())
        .map(VirtualWorkspaceTableEntity::toDomain);
  }

  @Override
  public Optional<VirtualWorkspace> findById(VirtualWorkspaceId id) {
    VirtualWorkspaceTableKey key =
        new VirtualWorkspaceTableKey(id.playerId().value(), id.stageCode().value());
    return jpaRepository.findById(key).map(VirtualWorkspaceTableEntity::toDomain);
  }

  @Override
  public VirtualWorkspace save(VirtualWorkspace workspace) {
    VirtualWorkspaceTableEntity entity = VirtualWorkspaceTableEntity.fromDomain(workspace);
    return jpaRepository.save(entity).toDomain();
  }
}
