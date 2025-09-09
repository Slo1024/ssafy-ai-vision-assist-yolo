package com.project.lookey.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Swagger UI가 기본적으로 우리 API를 로드하도록 리다이렉트
        registry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html?url=/v3/api-docs");
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html?url=/v3/api-docs");
    }
}