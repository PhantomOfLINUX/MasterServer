package org.codequistify.master.core.domain.stage.model;

import lombok.Getter;

@Getter
public enum AnswerType {
    MULTIPLE_CHOICE("M"),
    SHORT_ANSWER("S"),
    PRACTICAL("P"),
    DESCRIPTION("D");

    private final String code;

    AnswerType(String code) {
        this.code = code;
    }

    public boolean isStandard() {
        return this == MULTIPLE_CHOICE || this == SHORT_ANSWER;
    }
}
