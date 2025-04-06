package org.codequistify.master.infrastructure.stage.repository;

import org.codequistify.master.infrastructure.stage.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, String> {
    boolean existsByStageIdAndIndex(Long stageId, int questionIndex);

    Optional<QuestionEntity> findByStageIdAndIndex(Long stageId, Integer questionIndex);

    @Query("SELECT q.composable " +
            "FROM QuestionEntity q " +
            "WHERE q.stage.id = :stageId AND q.index = :questionIndex+1")
    Boolean isComposableForStageAndIndex(Long stageId, Integer questionIndex);

}
