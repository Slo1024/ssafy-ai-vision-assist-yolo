package com.project.lookey.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Getter
@Setter
public class ExternalApiConfig {

    private FoodSafety foodSafety = new FoodSafety();
    private Rda rda = new Rda();
    private GoogleMaps googleMaps = new GoogleMaps();
    private NaverMap naverMap = new NaverMap();

    @Getter
    @Setter
    public static class FoodSafety {
        private String url;
        private String key;
    }

    @Getter
    @Setter
    public static class Rda {
        private String url;
        private String key;
    }

    @Getter
    @Setter
    public static class GoogleMaps {
        private String url;
        private String key;
    }

    @Getter
    @Setter
    public static class NaverMap {
        private String url;
        private String clientId;
        private String clientSecret;
    }
}