package org.codequistify.master.domain.lab.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.codequistify.master.domain.lab.vo.LabServiceName;
import org.codequistify.master.domain.lab.vo.StageCode;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.global.util.BaseTimeEntity;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@IdClass(LabK8sRouteKey.class)
@Table(name = "lab_k8s_route")
public class VirtualWorkspaceEntity extends BaseTimeEntity {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Id
    @Column(name = "stage_code")
    private String stageCodeValue;

    @Column(name = "service_name", nullable = false, length = 63)
    private String serviceName;

    @Column(name = "service_dns", nullable = false)
    private String serviceDns;

    public static VirtualWorkspaceEntity create(LabRouteId routeId, LabServiceName serviceName, String namespace) {
        return VirtualWorkspaceEntity.builder()
                .playerId(routeId.playerId().value())
                .stageCodeValue(routeId.stageCode().value())
                .serviceName(serviceName.value())
                .serviceDns(serviceName.serviceDns(namespace))
                .build();
    }

    public LabRouteId routeId() {
        return LabRouteId.of(PlayerId.of(playerId), StageCode.from(stageCodeValue));
    }

    public LabServiceName serviceNameVo() {
        return LabServiceName.from(serviceName);
    }
}
