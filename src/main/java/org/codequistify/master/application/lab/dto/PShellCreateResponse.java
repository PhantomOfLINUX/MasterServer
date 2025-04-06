package org.codequistify.master.application.lab.dto;

import org.codequistify.master.core.domain.player.model.PolId;

public record PShellCreateResponse(
        String url,
        String query
) {
    public static PShellCreateResponse of(String url, PolId uid, String stageImageName) {
        String query = "?uid=" + uid.getValue().toLowerCase() + "&stage=" + stageImageName;
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
