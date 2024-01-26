package org.codequistify.master.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@OpenAPIDefinition(
//        info = @Info(title = "POL Master Server",
//                description = "POL의 마스터 서버 api 정보 명세서입니다.",
//                version = "v0.0.1"))
@Configuration
public class SwaggerConfig {
    @Value("${host.deploy.api.server}")
    String host;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(host)
                        .description("Production server"))
                .info(new Info().title("POL Master Server")
                        .description("POL의 마스터 서버 API 정보 명세서입니다.")
                        .version("v0.0.1"));

    }
}
