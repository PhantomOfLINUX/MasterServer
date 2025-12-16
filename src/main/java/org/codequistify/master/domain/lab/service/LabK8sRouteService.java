package org.codequistify.master.domain.lab.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.config.LabInfrastructureDefaults;
import org.codequistify.master.domain.lab.domain.LabK8sRoute;
import org.codequistify.master.domain.lab.domain.LabK8sRouteKey;
import org.codequistify.master.domain.lab.repository.LabK8sRouteRepository;
import org.codequistify.master.domain.lab.vo.LabRouteId;
import org.codequistify.master.domain.lab.vo.LabServiceName;
import org.codequistify.master.domain.lab.vo.LabUserUid;
import org.codequistify.master.domain.lab.vo.StageCode;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabK8sRouteService {
    private final LabK8sRouteRepository labK8sRouteRepository;

    public LabK8sRoute getOrCreate(PlayerId playerId, StageCode stageCode, LabUserUid uid) {
        LabRouteId routeId = LabRouteId.of(playerId, stageCode);
        return labK8sRouteRepository
                .findById(new LabK8sRouteKey(playerId.value(), stageCode.value()))
                .orElseGet(() -> issueRoute(routeId, stageCode, uid));
    }

    private LabK8sRoute issueRoute(LabRouteId routeId, StageCode stageCode, LabUserUid uid) {
        LabServiceName serviceName = LabServiceName.issue(stageCode, uid);
        LabK8sRoute route = LabK8sRoute.create(
                routeId,
                serviceName,
                LabInfrastructureDefaults.LAB_NAMESPACE
        );
        return labK8sRouteRepository.save(route);
    }
}
