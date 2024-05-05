package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum StageImageType {
    S1001("polhub/s1001", "latest"),
    S1002("polhub/s1002", "latest"),
    S1003("polhub/s1003", "latest"),
    S1004("polhub/s1004", "latest"),
    S1005("polhub/s1005", "latest"),
    S1006("polhub/s1006", "latest"),
    S1007("polhub/s1007", "latest"),
    S0000("polhub/s1001", "latest");

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
