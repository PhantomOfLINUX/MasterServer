package org.codequistify.master.domain.lab.vo;

import org.codequistify.master.global.data.Label;
import org.codequistify.master.global.data.Labels;

public class LabResourceLabels {
    private LabResourceLabels() {}

    public static Labels standard(LabResourceId resourceId) {
        return Labels.of(
                Label.of("app", "pol"),
                Label.of("tire", "term"),
                Label.of("player", resourceId.uid().value()),
                Label.of("stage", resourceId.stageCode().lowercase())
        );
    }
}
