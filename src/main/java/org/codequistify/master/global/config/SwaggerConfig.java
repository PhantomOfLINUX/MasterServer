package org.codequistify.master.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "POL Master Server",
                description = "POL의 마스터 서버 api 정보 명세서입니다.",
                version = "v0.0.1"))
@Configuration
public class SwaggerConfig {
}
