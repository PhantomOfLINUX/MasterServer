package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum StageImageType {
    STAGE001("polhub/stage01-ls", "latest"),
    STAGE002("jeongrae/pol_test", "v1");

    private final String image;
    private final String tag;

    StageImageType(String image, String tag) {
        this.image = image;
        this.tag = tag;
    }

    public String getImageName() {
        return this.image + this.tag;
    }
}
