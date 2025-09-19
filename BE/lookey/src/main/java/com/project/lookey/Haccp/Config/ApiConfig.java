package com.project.lookey.Haccp.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiConfig {

    @Value("${openapi.haccp.url}")
    private String apiUrl;

    @Value("${openapi.haccp.key}")
    private String apiKey;

    public String getApiUrl() { return apiUrl; }
    public String getApiKey() { return apiKey; }
}

