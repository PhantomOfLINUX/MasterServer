package org.codequistify.master.virtualworkspace.application.port;

import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;

public interface VirtualWorkspaceClient {
  void provision(VirtualWorkspace workspace);

  void deprovision(WorkspacePublicId publicId);

  void deprovisionSync(WorkspacePublicId publicId);

  boolean exists(WorkspacePublicId publicId);

  void waitUntilReady(WorkspacePublicId publicId);
}
