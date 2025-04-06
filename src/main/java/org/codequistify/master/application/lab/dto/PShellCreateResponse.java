package org.codequistify.master.application.lab.dto;

public record PShellCreateResponse(
        String url,
        String query
) {
    public static PShellCreateResponse of(String url, String uid, String stageImageName) {
        String query = "?uid=" + uid.toLowerCase() + "&stage=" + stageImageName;
        return new PShellCreateResponse(
                url + query,
                query
        );
    }

    public record XHeader(
            String key,
            String value
    ) {
    }
}
