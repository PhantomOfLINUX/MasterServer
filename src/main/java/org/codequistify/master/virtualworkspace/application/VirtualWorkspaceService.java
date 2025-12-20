package org.codequistify.master.virtualworkspace.application;

import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceDefaults;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceExternalEndpoints;
import org.codequistify.master.virtualworkspace.domain.model.StageSpecSnapshot;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceInternalRoute;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspacePublicEndpoint;
import org.codequistify.master.virtualworkspace.domain.model.WorkspaceAccessPolicy;
import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.virtualworkspace.infrastructure.k8s.VirtualWorkspaceKubernetesManager;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.repository.VirtualWorkspaceRepository;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceConnectionResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceStatusResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VirtualWorkspaceService {
  private final Logger logger = LoggerFactory.getLogger(VirtualWorkspaceService.class);

  private final VirtualWorkspaceRepository virtualWorkspaceRepository;
  private final VirtualWorkspaceKubernetesManager kubernetesManager;
  private final StageSearchService stageSearchService;

  public VirtualWorkspaceConnectionResponse recreate(String stageCode, Player player) {
    StageCode code = normalizeStageCode(stageCode);
    Stage stage = stageSearchService.getStageByCode(code);

    VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), code);
    SubjectId owner = SubjectId.from(player.getUsername());
    WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(owner);

    StageSpecSnapshot specSnapshot = snapshot(code, stage);

    Optional<VirtualWorkspace> existing = virtualWorkspaceRepository.findById(workspaceId);
    WorkspacePublicId newPublicId = WorkspacePublicId.issue();
    VirtualWorkspacePublicEndpoint newEndpoint =
        VirtualWorkspacePublicEndpoint.of(newPublicId, VirtualWorkspaceDefaults.BASE_HOST);

    VirtualWorkspace creating = existing
        .map(current -> current.recreate(newPublicId, specSnapshot, newEndpoint))
        .orElseGet(() -> VirtualWorkspace.creating(
            workspaceId, newPublicId, specSnapshot, newEndpoint, accessPolicy));

    virtualWorkspaceRepository.save(creating);

    try {
      existing.map(VirtualWorkspace::publicId).ifPresent(kubernetesManager::deleteSync);

      kubernetesManager.createService(creating);
      kubernetesManager.createPod(creating);
      kubernetesManager.waitForPodReadiness(newPublicId);
    } catch (RuntimeException e) {
      throw new BusinessException(
          ErrorCode.VIRTUAL_WORKSPACE_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    VirtualWorkspaceInternalRoute internalRoute = VirtualWorkspaceInternalRoute.of(
        VirtualWorkspaceDefaults.NAMESPACE, newPublicId.value(), specSnapshot.port());

    VirtualWorkspace running = creating.markRunning(internalRoute);
    running = virtualWorkspaceRepository.save(running);

    logger.info(
        "[recreate] stageCode: {}, workspaceId: {}, publicId: {}",
        stageCode,
        workspaceId,
        running.publicId().value());

    return connectionResponse(running);
  }

  public VirtualWorkspaceConnectionResponse getConnection(String stageCode, Player player) {
    StageCode code = normalizeStageCode(stageCode);
    VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), code);
    VirtualWorkspace workspace = virtualWorkspaceRepository
        .findById(workspaceId)
        .orElseThrow(() ->
            new BusinessException(ErrorCode.VIRTUAL_WORKSPACE_NOT_FOUND, HttpStatus.NOT_FOUND));

    return connectionResponse(workspace);
  }

  public VirtualWorkspaceStatusResponse getStatus(String stageCode, Player player) {
    StageCode code = normalizeStageCode(stageCode);
    VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), code);

    return virtualWorkspaceRepository
        .findById(workspaceId)
        .map(workspace -> {
          boolean exists = kubernetesManager.existsService(workspace.publicId())
              && kubernetesManager.existsPod(workspace.publicId());
          String status = workspace.lifecycle().status().name();
          return VirtualWorkspaceStatusResponse.of(workspace.publicId().value(), status, exists);
        })
        .orElseGet(() -> VirtualWorkspaceStatusResponse.of("", "NOT_FOUND", false));
  }

  public VirtualWorkspaceSummaryResponse getSummary(String stageCode, Player player) {
    VirtualWorkspaceStatusResponse statusResponse = getStatus(stageCode, player);
    return VirtualWorkspaceSummaryResponse.of(
        statusResponse.publicId(), statusResponse.status(), statusResponse.exists());
  }

  private StageSpecSnapshot snapshot(StageCode stageCode, Stage stage) {
    StageImageType stageImage = stage.getStageImage();
    String image = stageImage.getImageName();

    return StageSpecSnapshot.of(
        stageCode,
        image,
        VirtualWorkspaceDefaults.SERVICE_PORT,
        VirtualWorkspaceDefaults.DEFAULT_CPU,
        VirtualWorkspaceDefaults.DEFAULT_MEMORY,
        VirtualWorkspaceDefaults.READINESS_PATH);
  }

  private VirtualWorkspaceConnectionResponse connectionResponse(VirtualWorkspace workspace) {
    VirtualWorkspacePublicEndpoint endpoint = workspace.routing().publicEndpoint();
    String websocketUrl = VirtualWorkspaceExternalEndpoints.websocketUrl(endpoint);
    return VirtualWorkspaceConnectionResponse.of(
        workspace.publicId().value(), endpoint.value(), websocketUrl);
  }

  private StageCode normalizeStageCode(String stageCode) {
    return StageCode.from(stageCode.trim().toUpperCase(Locale.ROOT));
  }
}
