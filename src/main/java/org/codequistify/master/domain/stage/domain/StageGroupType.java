package org.codequistify.master.domain.stage.domain;

public enum StageGroupType {
    BASIC_PROBLEMS("B"),
    ADVANCED_PROBLEMS("A"),
    MOCK_TESTS("T");

    private final String code;

    StageGroupType(String code) {
        this.code = code;
    }
}
