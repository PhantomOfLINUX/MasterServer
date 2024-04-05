package org.codequistify.master.domain.lab.dto;

import java.util.List;

public record pShellCreateResponse(
        String url,
        List<XHeader> xHeaders
) {
    public static pShellCreateResponse of (String url, String uid, String stageImageName) {
        return new pShellCreateResponse(
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
