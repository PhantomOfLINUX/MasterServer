package org.codequistify.master.domain.lab.dto;

import org.codequistify.master.domain.lab.vo.LabUserUid;
import org.codequistify.master.domain.lab.vo.StageCode;
import org.codequistify.master.global.data.Pair;
import org.codequistify.master.global.data.UrlQuery;

public record VirtualWorkspaceCreateResponse(
        String url,
        String query
) {
    public static VirtualWorkspaceCreateResponse of(String url, UrlQuery query) {
        return new VirtualWorkspaceCreateResponse(
                url + query.value(),
                query.value()
        );
    }

    public static VirtualWorkspaceCreateResponse of(String url, LabUserUid uid, StageCode stageCode) {
        UrlQuery query = UrlQuery.from(
                Pair.of("uid", uid.value()),
                Pair.of("stage", stageCode.lowercase())
        );
        return of(url, query);
    }

    public static record XHeader (
            String key,
            String value
    ){}
}
