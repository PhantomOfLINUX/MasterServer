package org.codequistify.master.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {
    @Value("${host.deploy.api.server}")
    private String DEPLOY_HOST;

    @Value("${host.develop.api.server}")
    private String DEVELOP_HOST;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("POL Master Server")
                .description("POL의 마스터 서버 API 정보 명세서입니다.")
                .version("v0.0.1");

        Server develop = new Server()
                .url(DEVELOP_HOST)
                .description("Develop server");

        Server deploy = new Server()
                .url(DEPLOY_HOST)
                .description("Deploy server");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Authorization");

        return new OpenAPI()
                .addServersItem(develop)
                .addServersItem(deploy)
                .info(info)
                .components(new Components().addSecuritySchemes("Authorization", securityScheme))
                .security(Collections.singletonList(securityRequirement));

    }
}
