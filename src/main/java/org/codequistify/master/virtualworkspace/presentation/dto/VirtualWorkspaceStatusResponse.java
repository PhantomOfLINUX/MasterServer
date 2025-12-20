package org.codequistify.master.virtualworkspace.presentation.dto;

public record VirtualWorkspaceStatusResponse(String publicId, String status, boolean exists) {
  public static VirtualWorkspaceStatusResponse of(String publicId, String status, boolean exists) {
    return new VirtualWorkspaceStatusResponse(publicId, status, exists);
  }
}
