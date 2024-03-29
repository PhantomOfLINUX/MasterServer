package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.CompletedStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedStageRepository extends JpaRepository<CompletedStage, Long> {
}
