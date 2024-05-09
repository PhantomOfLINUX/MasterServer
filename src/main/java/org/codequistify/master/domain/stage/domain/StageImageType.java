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
    S1008("polhub/s1008", "latest"),
    S1009("polhub/s1009", "latest"),
    S1010("polhub/s1010", "latest"),
    S1011("polhub/s1011", "latest"),
    S1012("polhub/s1012", "latest"),
    S1013("polhub/s1013", "latest"),
    S1014("polhub/s1014", "latest"),
    S0000("polhub/s1015", "latest");

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
