package org.codequistify.master.domain.lab.dto;

import java.util.List;

public record PShellCreateResponse(
        String url,
        List<XHeader> xHeaders
) {
    public static PShellCreateResponse of (String url, String uid, String stageImageName) {
        return new PShellCreateResponse(
                url,
                List.of(
                        new XHeader("X-POL-UID", uid.toLowerCase()),
                        new XHeader("X-POL-STAGE", stageImageName)
                )
        );
    }

    public static record XHeader (
            String key,
            String value
    ){}
}
