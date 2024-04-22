package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.codequistify.master.domain.lab.utils.KubernetesResourceNaming;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.springframework.stereotype.Component;

@Component
public class KubernetesServiceFactory implements ServiceFactory{
    private final static Long ACTIVE_DEADLINE = 10_800L;
    @Override
    public Service create(Stage stage, int port, String uid) {
        StageImageType stageImage = stage.getStageImage();
        String serviceName = KubernetesResourceNaming.getServiceName(stageImage.name(), uid);

        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(serviceName)
                    .addToLabels("app", "pol")
                    .addToLabels("tire", "term")
                    .addToLabels("player", uid)
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
                    .addToSelector("player", uid)
                .endSpec().build();
    }

    private String generateServiceName(StageImageType stageImage, String uid) {
        return stageImage.name().toLowerCase() + "-svc-" + uid;
    }
}
