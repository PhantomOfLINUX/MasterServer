package org.codequistify.master.virtualworkspace.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubjectIdTest {

    @Test
    void subjectId가_빈값인_경우에는_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> SubjectId.from(" "));
    }

    @Test
    void subjectId가_존재하면_생성되어야한다() {
        assertDoesNotThrow(() -> SubjectId.from("user-subject-123"));
    }
}

