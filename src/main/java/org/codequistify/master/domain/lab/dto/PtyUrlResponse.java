package org.codequistify.master.domain.lab.dto;

public record PtyUrlResponse(
        String connectionURL
) {
    public static PtyUrlResponse of(String connectionURL) {
        return new PtyUrlResponse(connectionURL);
    }
}
