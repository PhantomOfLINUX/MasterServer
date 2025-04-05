package org.codequistify.master.core.domain.player.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.core.domain.exception.CoreException;

@Getter
@ToString
@EqualsAndHashCode
public class PolId {
    private final String value;

    private PolId(String value) {
        this.value = value;
    }

    public static PolId of(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException("PolId는 null이거나 빈 문자열일 수 없습니다.");
        }
        return new PolId(value);
    }
}
