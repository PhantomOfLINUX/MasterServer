package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
}
