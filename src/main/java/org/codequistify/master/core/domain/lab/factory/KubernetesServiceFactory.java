package org.codequistify.master.core.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.codequistify.master.core.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.codequistify.master.core.domain.stage.domain.StageImageType;
import org.springframework.stereotype.Component;

@Component
public class KubernetesServiceFactory implements ServiceFactory {

    private static final long ACTIVE_DEADLINE = 10_800L;

    @Override
    public Service create(Stage stage, int port, PolId uid) {
        StageImageType stageImage = stage.getStageImage();

        String lowerUid = uid.getValue().toLowerCase();
        String lowerStageName = stageImage.name().toLowerCase();
        String serviceName = KubernetesResourceNaming.getServiceName(stageImage.name(), uid);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(serviceName)
                .addToLabels("app", "pol")
                .addToLabels("tire", "term")
                .addToLabels("player", lowerUid)
                .addToLabels("stage", lowerStageName)
                .endMetadata()
                .withNewSpec()
                .withType("ClusterIP")
                .addNewPort()
                .withName("http")
                .withProtocol("TCP")
                .withPort(port)
                .withTargetPort(new IntOrString(port))
                .endPort()
                .addToSelector("app", "pol")
                .addToSelector("tire", "term")
                .addToSelector("player", lowerUid)
                .addToSelector("stage", lowerStageName)
                .endSpec()
                .build();
    }
}
