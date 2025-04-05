package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.core.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.codequistify.master.core.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final static Long ACTIVE_DEADLINE = 10_800L;
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);

    @Override
    public Pod create(Stage stage, int port, String uid) {
        StageImageType stageImage = stage.getStageImage();
        String podName = KubernetesResourceNaming.getPodName(stageImage.name(), uid);

        return new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .addToLabels("app", "pol")
                .addToLabels("tire", "term")
                .addToLabels("player", uid)
                .addToLabels("stage", stageImage.name().toLowerCase())
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
                .withPath("/health")
                .withPort(new IntOrString(8080))
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
