package org.codequistify.master.virtualworkspace.application;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceDefaults;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceExternalEndpoints;
import org.codequistify.master.virtualworkspace.domain.model.StageSpecSnapshot;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceInternalRoute;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspacePublicEndpoint;
import org.codequistify.master.virtualworkspace.domain.model.WorkspaceAccessPolicy;
import org.codequistify.master.virtualworkspace.dto.VirtualWorkspaceConnectResponse;
import org.codequistify.master.virtualworkspace.dto.VirtualWorkspaceExistenceResponse;
import org.codequistify.master.virtualworkspace.infrastructure.k8s.VirtualWorkspaceKubernetesManager;
import org.codequistify.master.virtualworkspace.infrastructure.persistence.repository.VirtualWorkspaceRepository;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VirtualWorkspaceService {
    private final Logger logger = LoggerFactory.getLogger(VirtualWorkspaceService.class);

    private final VirtualWorkspaceRepository virtualWorkspaceRepository;
    private final VirtualWorkspaceKubernetesManager kubernetesManager;
    private final StageSearchService stageSearchService;

    public VirtualWorkspaceConnectResponse recreate(Long stageId, Player player) {
        Stage stage = stageSearchService.getStageById(stageId);

        VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), StageCode.from(stage.getStageImage().name()));
        SubjectId owner = SubjectId.from(player.getUsername());
        WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(owner);

        StageSpecSnapshot specSnapshot = snapshot(stage);

        Optional<VirtualWorkspace> existing = virtualWorkspaceRepository.findById(workspaceId);
        WorkspacePublicId newPublicId = WorkspacePublicId.issue();
        VirtualWorkspacePublicEndpoint newEndpoint = VirtualWorkspacePublicEndpoint.of(newPublicId, VirtualWorkspaceDefaults.BASE_HOST);

        VirtualWorkspace creating = existing
                .map(current -> current.recreate(newPublicId, specSnapshot, newEndpoint))
                .orElseGet(() -> VirtualWorkspace.creating(workspaceId, newPublicId, specSnapshot, newEndpoint, accessPolicy));

        virtualWorkspaceRepository.save(creating);

        try {
            existing
                    .map(VirtualWorkspace::publicId)
                    .ifPresent(kubernetesManager::deleteSync);

            kubernetesManager.createService(creating);
            kubernetesManager.createPod(creating);
            kubernetesManager.waitForPodReadiness(newPublicId);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.VIRTUAL_WORKSPACE_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        VirtualWorkspaceInternalRoute internalRoute = VirtualWorkspaceInternalRoute.of(
                VirtualWorkspaceDefaults.NAMESPACE,
                newPublicId.value(),
                specSnapshot.port()
        );

        VirtualWorkspace running = creating.markRunning(internalRoute);
        running = virtualWorkspaceRepository.save(running);

        logger.info("[recreate] stageId: {}, workspaceId: {}, publicId: {}", stageId, workspaceId, running.publicId().value());

        return VirtualWorkspaceConnectResponse.of(
                VirtualWorkspaceExternalEndpoints.websocketUrl(running.routing().publicEndpoint()),
                running.publicId().value()
        );
    }

    public VirtualWorkspaceConnectResponse getAccessUrl(Long stageId, Player player) {
        Stage stage = stageSearchService.getStageById(stageId);

        VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), StageCode.from(stage.getStageImage().name()));
        VirtualWorkspace workspace = virtualWorkspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIRTUAL_WORKSPACE_NOT_FOUND, HttpStatus.NOT_FOUND));

        return VirtualWorkspaceConnectResponse.of(
                VirtualWorkspaceExternalEndpoints.websocketUrl(workspace.routing().publicEndpoint()),
                workspace.publicId().value()
        );
    }

    public VirtualWorkspaceExistenceResponse checkExistence(Long stageId, Player player) {
        Stage stage = stageSearchService.getStageById(stageId);
        VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(player.id(), StageCode.from(stage.getStageImage().name()));

        return virtualWorkspaceRepository.findById(workspaceId)
                .map(workspace -> {
                    boolean exists = kubernetesManager.existsService(workspace.publicId())
                            && kubernetesManager.existsPod(workspace.publicId());
                    return VirtualWorkspaceExistenceResponse.of(workspace.publicId().value(), exists);
                })
                .orElseGet(() -> VirtualWorkspaceExistenceResponse.of("", false));
    }

    private StageSpecSnapshot snapshot(Stage stage) {
        StageImageType stageImage = stage.getStageImage();
        String image = stageImage.getImageName();

        return StageSpecSnapshot.of(
                StageCode.from(stageImage.name()),
                image,
                VirtualWorkspaceDefaults.SERVICE_PORT,
                VirtualWorkspaceDefaults.DEFAULT_CPU,
                VirtualWorkspaceDefaults.DEFAULT_MEMORY,
                VirtualWorkspaceDefaults.READINESS_PATH
        );
    }
}
