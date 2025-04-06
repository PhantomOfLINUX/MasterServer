package org.codequistify.master.core.domain.player.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codequistify.master.core.exception.CoreException;

@EqualsAndHashCode
@ToString
public class HashedPassword {

    private final String value;

    private HashedPassword(String value) {
        this.value = value;
    }

    public static HashedPassword fromHashed(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new CoreException("비밀번호는 null이 될 수 없습니다.");
        }
        return new HashedPassword(hashed);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}