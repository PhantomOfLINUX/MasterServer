package org.codequistify.master.domain.lab.factory;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.codequistify.master.domain.lab.vo.Label;
import org.springframework.stereotype.Component;

@Component
public class KubernetesServiceFactory implements ServiceFactory{
    @Override
    public Service create(String name, int port, int targetPort, Label selector) {
        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(name)
                    .addToLabels(selector.key(), selector.value())
                .endMetadata()
                .withNewSpec()
                    .withType("NodePort")
                    .addNewPort()
                        .withName("http")
                        .withProtocol("TCP")
                        .withPort(port)
                    .withTargetPort(new IntOrString(targetPort))
                    .endPort()
                    .addToSelector(selector.key(), selector.value())
                .endSpec().build();
    }
}
