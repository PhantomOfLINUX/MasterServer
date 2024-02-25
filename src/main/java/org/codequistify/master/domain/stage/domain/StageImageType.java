package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum StageImageType {
    STAGE001("jeongrae/pol_test", "v1"),
    STAGE002("jeongrae/pol_test", "v1");

    private final String name;
    private final String version;

    StageImageType(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getImageName() {
        return this.name + this.version;
    }
}
