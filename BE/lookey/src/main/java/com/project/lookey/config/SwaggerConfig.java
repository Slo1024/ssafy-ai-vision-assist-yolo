package com.project.lookey.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;

@Configuration
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
                .tags(List.of(
                        new Tag().name("Common").description("공통 API (Health Check, Echo 등)"),
                        new Tag().name("Auth").description("인증 관련 API (로그인, 회원가입, JWT 등)"),
                        new Tag().name("Allergy").description("알레르기 정보 관련 API"),
                        new Tag().name("Cart").description("장바구니 관련 API")
                ))
                .components(new Components());
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
                    new Server().url("https://j13e101.p.ssafy.io/dev").description("Development Server"),
                    new Server().url("http://localhost:8080").description("Local Development"),
                    new Server().url("https://j13e101.p.ssafy.io").description("Production Server")
            );
        }
    }
}