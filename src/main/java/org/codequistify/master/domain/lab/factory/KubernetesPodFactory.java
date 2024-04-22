package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.codequistify.master.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KubernetesPodFactory implements PodFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(KubernetesPodFactory.class);
    private final static Long ACTIVE_DEADLINE = 10_800L;

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
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName(stageImage.name().toLowerCase())
                        .withImage(stageImage.getImageName())
                        .addNewPort()
                            .withContainerPort(port)
                        .endPort()
                        .withNewReadinessProbe()// agent 준비 확인
                            .withNewTcpSocket()
                                .withPort(new IntOrString(8080))
                            .endTcpSocket()
                            .withInitialDelaySeconds(9)
                            .withPeriodSeconds(1)
                        .endReadinessProbe()
                    .endContainer()
                    .withActiveDeadlineSeconds(ACTIVE_DEADLINE)
                .endSpec().build();
    }

    private String generatePodName(StageImageType stageImage, String uid) {
        return stageImage.name().toLowerCase() + "-pod-" + uid;
    }
}
