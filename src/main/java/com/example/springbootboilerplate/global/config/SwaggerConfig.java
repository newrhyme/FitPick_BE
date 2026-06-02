package com.example.springbootboilerplate.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("Boilerplate API 문서")
                .description("Auth/JWT 기반 Boilerplate API 문서")
                .version("v1")
        );
    }
}
