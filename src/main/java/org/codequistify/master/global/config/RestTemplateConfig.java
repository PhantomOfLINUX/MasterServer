package org.codequistify.master.global.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(60)
                .build();

        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
        factory.setConnectTimeout(1000);
        factory.setConnectionRequestTimeout(5000);

        return new RestTemplate(factory);
    }
}
