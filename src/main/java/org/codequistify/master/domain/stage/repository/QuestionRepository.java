package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    boolean existsByIndex(int questionIndex);
}
