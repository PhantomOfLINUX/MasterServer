package org.codequistify.master.global.data;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record UrlQuery(List<Pair<String, String>> parameters) {
    private static final String PREFIX = "?";
    private static final String SEPARATOR = "&";
    private static final String KEY_VALUE_DELIMITER = "=";

    public UrlQuery {
        Objects.requireNonNull(parameters, "parameters must not be null");
    }

    public static UrlQuery from(Pair<String, String>... parameters) {
        return new UrlQuery(Arrays.stream(parameters).toList());
    }

    public String value() {
        if (parameters.isEmpty()) {
            return "";
        }

        String query = parameters.stream()
                .map(param -> param.first() + KEY_VALUE_DELIMITER + param.second())
                .collect(Collectors.joining(SEPARATOR));

        return PREFIX + query;
    }

    @Override
    public String toString() {
        return value();
    }
}
