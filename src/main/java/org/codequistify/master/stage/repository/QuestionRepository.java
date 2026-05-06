package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    boolean existsByStageIdAndIndex(Long stageId, int questionIndex);

    Optional<Question> findByStageIdAndIndex(Long stageId, Integer questionIndex);

    @Query("SELECT q.isComposable " +
            "FROM Question q " +
            "WHERE q.stage.id = :stageId AND q.index = :questionIndex+1")
    Boolean isComposableForStageAndIndex(Long stageId, Integer questionIndex);

}
