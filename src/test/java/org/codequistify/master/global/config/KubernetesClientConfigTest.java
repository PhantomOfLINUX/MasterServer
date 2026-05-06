package org.codequistify.master.global.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesClientConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KubernetesClientConfig.class);

    @Test
    void localProfileUsesKubernetesMockServerClient() {
        contextRunner
                .withPropertyValues("spring.profiles.active=local")
                .run(context -> {
                    assertThat(context).hasSingleBean(KubernetesServer.class);
                    assertThat(context).hasSingleBean(KubernetesClient.class);

                    KubernetesServer kubernetesServer = context.getBean(KubernetesServer.class);
                    KubernetesClient kubernetesClient = context.getBean(KubernetesClient.class);

                    assertThat(kubernetesClient).isSameAs(kubernetesServer.getClient());
                });
    }
}
