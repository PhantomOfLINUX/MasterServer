package org.codequistify.master.virtualworkspace.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.codequistify.master.virtualworkspace.domain.model.StageSpecSnapshot;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceInternalRoute;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceLifecycle;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspacePublicEndpoint;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceRouting;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceStatus;
import org.codequistify.master.virtualworkspace.domain.model.WorkspaceAccessMode;
import org.codequistify.master.virtualworkspace.domain.model.WorkspaceAccessPolicy;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.global.util.BaseTimeEntity;

import java.util.Optional;

@Accessors(fluent = true)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@IdClass(VirtualWorkspaceTableKey.class)
@Table(
        name = "virtual_workspace",
        indexes = {
                @Index(name = "uk_virtual_workspace_public_id", columnList = "public_id", unique = true)
        }
)
public class VirtualWorkspaceTableEntity extends BaseTimeEntity {

    @Id
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Id
    @Column(name = "stage_code", nullable = false, length = 32)
    private String stageCode;

    @Column(name = "public_id", nullable = false, length = 63)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    private VirtualWorkspaceStatus lifecycleStatus;

    @Column(name = "owner_subject_id", nullable = false, length = 255)
    private String ownerSubjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_mode", nullable = false, length = 20)
    private WorkspaceAccessMode accessMode;

    @Column(name = "base_host", nullable = false, length = 255)
    private String baseHost;

    @Column(name = "k8s_namespace", length = 63)
    private String k8sNamespace;

    @Column(name = "service_name", length = 63)
    private String serviceName;

    @Column(name = "service_port")
    private Integer servicePort;

    @Column(name = "image", nullable = false, length = 255)
    private String image;

    @Column(name = "port", nullable = false)
    private int port;

    @Column(name = "cpu", nullable = false, length = 32)
    private String cpu;

    @Column(name = "memory", nullable = false, length = 32)
    private String memory;

    @Column(name = "readiness_path", nullable = false, length = 128)
    private String readinessPath;

    public static VirtualWorkspaceTableEntity fromDomain(VirtualWorkspace workspace) {
        StageSpecSnapshot spec = workspace.specSnapshot();
        VirtualWorkspaceRouting routing = workspace.routing();

        Optional<VirtualWorkspaceInternalRoute> internalRoute = routing.internalRoute();

        return VirtualWorkspaceTableEntity.builder()
                .playerId(workspace.id().playerId().value())
                .stageCode(workspace.id().stageCode().value())
                .publicId(workspace.publicId().value())
                .lifecycleStatus(workspace.lifecycle().status())
                .ownerSubjectId(workspace.accessPolicy().owner().value())
                .accessMode(workspace.accessPolicy().mode())
                .baseHost(routing.publicEndpoint().baseHost())
                .k8sNamespace(internalRoute.map(VirtualWorkspaceInternalRoute::namespace).orElse(null))
                .serviceName(internalRoute.map(VirtualWorkspaceInternalRoute::serviceName).orElse(null))
                .servicePort(internalRoute.map(VirtualWorkspaceInternalRoute::servicePort).orElse(null))
                .image(spec.image())
                .port(spec.port())
                .cpu(spec.cpu())
                .memory(spec.memory())
                .readinessPath(spec.readinessPath())
                .build();
    }

    public VirtualWorkspace toDomain() {
        VirtualWorkspaceId id = VirtualWorkspaceId.of(
                PlayerId.of(playerId),
                StageCode.from(stageCode)
        );

        WorkspacePublicId publicIdVo = WorkspacePublicId.from(publicId);
        VirtualWorkspaceLifecycle lifecycle = new VirtualWorkspaceLifecycle(lifecycleStatus);

        StageSpecSnapshot spec = StageSpecSnapshot.of(
                StageCode.from(stageCode),
                image,
                port,
                cpu,
                memory,
                readinessPath
        );

        VirtualWorkspacePublicEndpoint endpoint = VirtualWorkspacePublicEndpoint.of(publicIdVo, baseHost);

        VirtualWorkspaceRouting routing = internalRoute()
                .<VirtualWorkspaceRouting>map(route -> VirtualWorkspaceRouting.routed(endpoint, route))
                .orElseGet(() -> VirtualWorkspaceRouting.pending(endpoint));

        WorkspaceAccessPolicy accessPolicy = new WorkspaceAccessPolicy(
                SubjectId.from(ownerSubjectId),
                accessMode
        );

        return new VirtualWorkspace(id, publicIdVo, lifecycle, spec, routing, accessPolicy);
    }

    private Optional<VirtualWorkspaceInternalRoute> internalRoute() {
        if (k8sNamespace == null || serviceName == null || servicePort == null) {
            return Optional.empty();
        }
        return Optional.of(VirtualWorkspaceInternalRoute.of(k8sNamespace, serviceName, servicePort));
    }
}
