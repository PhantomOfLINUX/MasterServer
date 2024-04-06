package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);

    @Override
    public Pod create(Stage stage, int port, String uid) {
        StageImageType stageImage = stage.getStageImage();
        String podName = generatePodName(stageImage, uid);

        return new PodBuilder()
                .withNewMetadata()
                    .withName(podName)
                    .addToLabels("app", "pol")
                    .addToLabels("tire", "term")
                    .addToLabels("player", uid)
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName(stageImage.name().toLowerCase())
                        .withImage(stageImage.getImageName())
                        .addNewPort()
                            .withContainerPort(port)
                        .endPort()
                    .endContainer()
                .endSpec().build();
    }

    private String generatePodName(StageImageType stageImage, String uid) {
        return stageImage.name().toLowerCase() + "-pod-" + uid;
    }
}
