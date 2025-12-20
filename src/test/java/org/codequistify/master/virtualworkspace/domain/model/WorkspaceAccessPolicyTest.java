package org.codequistify.master.virtualworkspace.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.junit.jupiter.api.Test;

class WorkspaceAccessPolicyTest {

  @Test
  void ownerOnly는_OWNER_ONLY_모드여야한다() {
    SubjectId owner = SubjectId.from("owner-1");

    WorkspaceAccessPolicy policy = WorkspaceAccessPolicy.ownerOnly(owner);

    assertEquals(WorkspaceAccessMode.OWNER_ONLY, policy.mode());
    assertEquals(owner, policy.owner());
  }

  @Test
  void adminOnly는_ADMIN_ONLY_모드여야한다() {
    SubjectId owner = SubjectId.from("owner-1");

    WorkspaceAccessPolicy policy = WorkspaceAccessPolicy.adminOnly(owner);

    assertEquals(WorkspaceAccessMode.ADMIN_ONLY, policy.mode());
    assertEquals(owner, policy.owner());
  }

  @Test
  void owner가_null이면_생성이_실패해야한다() {
    assertThrows(NullPointerException.class, () -> new WorkspaceAccessPolicy(null, WorkspaceAccessMode.OWNER_ONLY));
  }
}
