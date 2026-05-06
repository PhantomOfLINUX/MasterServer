package org.codequistify.master.global.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class KubernetesClientConfig {

    @Bean(destroyMethod = "close")
    @Profile("!local")
    public KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder().build();
    }

    @Bean(initMethod = "before", destroyMethod = "after")
    @Profile("local")
    public KubernetesServer kubernetesServer() {
        return new KubernetesServer(false, true);
    }

    @Bean(destroyMethod = "")
    @Profile("local")
    public KubernetesClient localKubernetesClient(KubernetesServer kubernetesServer) {
        return kubernetesServer.getClient();
    }
}
