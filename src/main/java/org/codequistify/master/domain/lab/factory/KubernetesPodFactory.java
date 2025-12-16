package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.domain.lab.config.LabInfrastructureDefaults;
import org.codequistify.master.domain.lab.vo.KubernetesResourceName;
import org.codequistify.master.domain.lab.vo.LabResourceId;
import org.codequistify.master.domain.lab.vo.LabResourceLabels;
import org.codequistify.master.global.data.Labels;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);
    private final static Long ACTIVE_DEADLINE = 10_800L;

    @Override
    public Pod create(LabResourceId resourceId, int port) {
        StageImageType stageImage = resourceId.stage().getStageImage();
        KubernetesResourceName resourceName = resourceId.resourceName();
        Labels labels = LabResourceLabels.standard(resourceId);

        return new PodBuilder()
                .withNewMetadata()
                    .withName(resourceName.podName())
                    .withLabels(labels.toSingleValueMap())
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName(stageImage.name().toLowerCase())
                        .withImage(stageImage.getImageName())
                        .addNewPort()
                            .withContainerPort(port)
                        .endPort()
                        .withNewReadinessProbe()// agent 준비 확인
                            .withNewHttpGet()
                                .withPath(LabInfrastructureDefaults.LAB_READINESS_PATH)
                                .withPort(new IntOrString(LabInfrastructureDefaults.LAB_READINESS_PORT))
                            .endHttpGet()
                            .withInitialDelaySeconds(9)
                            .withPeriodSeconds(2)
                        .endReadinessProbe()
                    .endContainer()
                    .withActiveDeadlineSeconds(ACTIVE_DEADLINE)
                .endSpec().build();
    }

    private String generatePodName(StageImageType stageImage, String uid) {
        return stageImage.name().toLowerCase() + "-pod-" + uid;
    }
}
