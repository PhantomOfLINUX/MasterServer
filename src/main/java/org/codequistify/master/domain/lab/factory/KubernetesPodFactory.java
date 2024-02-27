package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.domain.lab.vo.Label;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);

    @Override
    public Pod create(String name, StageImageType imageType, int port, Label selector) {
        return new PodBuilder()
                .withNewMetadata()
                    .withGenerateName(name)
                    .addToLabels(selector.key(), selector.value())
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName(imageType.name())
                        .withImage(imageType.getImageName())
                        .addNewPort()
                            .withContainerPort(port)
                        .endPort()
                    .endContainer()
                .endSpec().build();
    }
}
