package org.codequistify.master.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.regex.Pattern;

@ToString
@EqualsAndHashCode
public class Email {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Email of(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        return new Email(value);
    }

    public static boolean isValid(String value) {
        return value != null && EMAIL_REGEX.matcher(value).matches();
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
