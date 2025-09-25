package com.project.lookey.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lookey API")
                        .description("음식 알레르기 정보 제공 서비스 API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SSAFY 13기 E101")
                                .url("https://j13e101.p.ssafy.io")
                        )
                )
                .servers(getServers())
                .components(new Components())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("lookey-api")
                .pathsToMatch("/api/**")
                .build();
    }

    private List<Server> getServers() {
        if ("prod".equals(activeProfile)) {
            return List.of(
                    new Server().url("https://j13e101.p.ssafy.io").description("Production Server"),
                    new Server().url("https://j13e101.p.ssafy.io/dev").description("Development Server"),
                    new Server().url("http://localhost:8080").description("Local Development")
            );
        } else {
            return List.of(
                    new Server().url("http://j13e101.p.ssafy.io:8082").description("Development Server"),
                    new Server().url("http://localhost:8080").description("Local Development"),
                    new Server().url("http://j13e101.p.ssafy.io:8081").description("Production Server")
            );
        }
    }
}