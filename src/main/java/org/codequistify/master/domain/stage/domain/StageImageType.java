package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum StageImageType {
    STAGE01("polhub/stage01-ls", "latest"),
    STAGE02("polhub/stage02-cd-pwd", "latest");

    private final String image;
    private final String tag;

    StageImageType(String image, String tag) {
        this.image = image;
        this.tag = tag;
    }

    public String getImageName() {
        return this.image + ":" + this.tag;
    }
}
