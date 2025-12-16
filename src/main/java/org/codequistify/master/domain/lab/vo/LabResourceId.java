package org.codequistify.master.domain.lab.vo;

import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;

import java.util.Objects;

public record LabResourceId(
        Stage stage,
        StageCode stageCode,
        LabUserUid uid,
        LabServiceName serviceName
) {
    public LabResourceId {
        Objects.requireNonNull(stage, "stage must not be null");
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(uid, "uid must not be null");
        Objects.requireNonNull(serviceName, "serviceName must not be null");
    }

    public static LabResourceId from(Stage stage, Player player, LabServiceName serviceName) {
        return new LabResourceId(stage, StageCode.from(stage), LabUserUid.from(player), serviceName);
    }

    public static LabResourceId from(Stage stage, String playerUid, LabServiceName serviceName) {
        return new LabResourceId(stage, StageCode.from(stage), LabUserUid.from(playerUid), serviceName);
    }

    public KubernetesResourceName resourceName() {
        return KubernetesResourceName.of(stageCode, uid, serviceName);
    }
}
