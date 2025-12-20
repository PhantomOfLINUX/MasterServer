package org.codequistify.master.virtualworkspace.application.port;

import java.util.Optional;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;

public interface VirtualWorkspaceRepository {
  Optional<VirtualWorkspace> findByPublicId(WorkspacePublicId publicId);

  Optional<VirtualWorkspace> findById(VirtualWorkspaceId id);

  VirtualWorkspace save(VirtualWorkspace workspace);
}
