package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum StageImageType {
    S1001("polhub/stage01-ls", "v1"),
    S1002("polhub/stage02-cd-pwd", "latest"),
    S1003("jeongrae/socket","latest"),
    S0000("","");

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
