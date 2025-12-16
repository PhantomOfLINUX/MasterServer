package org.codequistify.master.domain.lab.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabServiceNameTest {

    @Test
    void stageCode와_uid로_서비스명을_발급한다() {
        StageCode stageCode = StageCode.from("S1001");
        LabUserUid uid = LabUserUid.from("USER-ABC");

        LabServiceName serviceName = LabServiceName.issue(stageCode, uid);

        assertThat(serviceName.value()).isEqualTo("s1001-user-abc");
    }

    @Test
    void 서비스명이_63자를_넘으면_잘라낸다() {
        StageCode stageCode = StageCode.from("S1001");
        LabUserUid uid = LabUserUid.from("a".repeat(80));

        LabServiceName serviceName = LabServiceName.issue(stageCode, uid);

        assertThat(serviceName.value().length()).isLessThanOrEqualTo(63);
        assertThat(serviceName.value()).doesNotEndWith("-");
    }

    @Test
    void DNS형식이_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> LabServiceName.from("INVALID*NAME"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("형식");
    }

    @Test
    void 서비스DNS에_네임스페이스가_포함된다() {
        LabServiceName serviceName = LabServiceName.from("svc-sample");

        String dns = serviceName.serviceDns("lab");

        assertThat(dns).isEqualTo("svc-sample.lab.svc.cluster.local");
    }
}
