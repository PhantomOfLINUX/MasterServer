package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);

    private String generatePodName(StageImageType stageImageType, String seq) {
        return stageImageType.name().toLowerCase()+"-pod-"+seq;
    }

    @Override
    public Pod createPod(StageImageType imageType, int port, String seq) {
        return new PodBuilder()
                .withNewMetadata()
                    .withGenerateName(generatePodName(imageType, seq))
                    .addToLabels("stage", imageType.name())
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName(generatePodName(imageType, seq))
                        .withImage(imageType.getImageName())
                        .addNewPort()
                            .withContainerPort(8080)
                        .endPort()
                    .endContainer()
                .endSpec().build();
    }
}
