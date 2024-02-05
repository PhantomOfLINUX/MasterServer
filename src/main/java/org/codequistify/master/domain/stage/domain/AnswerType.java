package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum AnswerType {
    MULTIPLE_CHOICE("01"),
    SHORT_ANSWER("02"),
    PRACTICAL("03"),
    DESCRIPTION("04");

    private final String code;

    AnswerType(String code) {
        this.code = code;
    }
}
