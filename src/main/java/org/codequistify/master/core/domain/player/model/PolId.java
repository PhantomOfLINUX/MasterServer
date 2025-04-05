package org.codequistify.master.core.domain.player.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codequistify.master.core.domain.exception.CoreException;

@ToString
@EqualsAndHashCode
public class PolId {
    private final String value;

    private PolId(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PolId of(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException("PolId는 null이거나 빈 문자열일 수 없습니다.");
        }
        return new PolId(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
