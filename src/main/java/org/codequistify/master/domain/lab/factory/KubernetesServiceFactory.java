package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.codequistify.master.domain.lab.vo.KubernetesResourceName;
import org.codequistify.master.domain.lab.vo.LabResourceId;
import org.codequistify.master.domain.lab.vo.LabResourceLabels;
import org.codequistify.master.global.data.Labels;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.springframework.stereotype.Component;

@Component
public class KubernetesServiceFactory implements ServiceFactory{
    @Override
    public Service create(LabResourceId resourceId, int port) {
        StageImageType stageImage = resourceId.stage().getStageImage();
        KubernetesResourceName resourceName = resourceId.resourceName();
        Labels labels = LabResourceLabels.standard(resourceId);

        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(resourceName.serviceName())
                    .withLabels(labels.toSingleValueMap())
                .endMetadata()
                .withNewSpec()
                    .withType("ClusterIP")
                    .addNewPort()
                        .withName("http")
                        .withProtocol("TCP")
                        .withPort(port)
                    .withTargetPort(new IntOrString(port))
                    .endPort()
                    .withSelector(labels.toSingleValueMap())
                .endSpec().build();
    }

    private String generateServiceName(StageImageType stageImage, String uid) {
        return stageImage.name().toLowerCase() + "-svc-" + uid;
    }
}
