package org.codequistify.master.yumin.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByIdStartingWith(String code);

    @Query("SELECT t FROM Task t WHERE t.id LIKE %:code%")
    List<Task> findByIdContainingCode(@Param("code") String code);
}
