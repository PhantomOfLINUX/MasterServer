package org.codequistify.master.domain.stage.domain;

public enum StageGroupType {
    BASIC_PROBLEMS("B"),
    ADVANCED_PROBLEMS_ACCESS("A"),
    MOCK_TESTS_ACCESS("T");

    private final String code;

    StageGroupType(String code) {
        this.code = code;
    }
}
