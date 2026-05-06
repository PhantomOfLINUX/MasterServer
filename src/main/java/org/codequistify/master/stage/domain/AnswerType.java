package org.codequistify.master.domain.stage.domain;

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
}
