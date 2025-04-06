package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.core.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.StageImageType;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {

    private static final long ACTIVE_DEADLINE = 10_800L;
    private final Logger logger = LoggerFactory.getLogger(KubernetesPodFactory.class);

    @Override
    public Pod create(StageEntity stageEntity, int port, PolId uid) {
        StageImageType stageImage = stageEntity.getStageImage();
        String lowerUid = uid.getValue().toLowerCase();
        String imageName = stageImage.name().toLowerCase();
        String podName = KubernetesResourceNaming.getPodName(stageImage.name(), uid);

        return new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .addToLabels("app", "pol")
                .addToLabels("tire", "term")
                .addToLabels("player", lowerUid)
                .addToLabels("stageEntity", imageName)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(imageName)
                .withImage(stageImage.getImageName())
                .addNewPort()
                .withContainerPort(port)
                .endPort()
                .withNewReadinessProbe()
                .withNewHttpGet()
                .withPath("/health")
                .withPort(new IntOrString(8080))
                .endHttpGet()
                .withInitialDelaySeconds(9)
                .withPeriodSeconds(2)
                .endReadinessProbe()
                .endContainer()
                .withActiveDeadlineSeconds(ACTIVE_DEADLINE)
                .endSpec()
                .build();
    }
}
