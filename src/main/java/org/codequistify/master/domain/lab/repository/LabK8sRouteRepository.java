package org.codequistify.master.domain.lab.repository;

import org.codequistify.master.domain.lab.domain.LabK8sRoute;
import org.codequistify.master.domain.lab.domain.LabK8sRouteKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabK8sRouteRepository extends JpaRepository<LabK8sRoute, LabK8sRouteKey> {
}
