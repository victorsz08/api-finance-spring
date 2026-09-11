package com.appfinace.api.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String securityConfigName = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AppFinance API")
                        .description("Api de gerenciamento financeiro")
                        .version("V1"))
                .components(new Components()
                        .addSecuritySchemes(securityConfigName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access_token")));
    }
}
