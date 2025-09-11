package com.project.lookey.product.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class FoodSafetyClient {
    private final WebClient web = WebClient.builder()
            .baseUrl("http://openapi.foodsafetykorea.go.kr")
            .build();

    @Value("${foodsafety.key}") private String key;

    /** C002: 품목제조보고(원재료) */
    public Mono<String> c002(int start, int end, String productName) {
        String p = URLEncoder.encode(productName, StandardCharsets.UTF_8);
        String path = String.format("/api/%s/C002/json/%d/%d/PRDLST_NM=%s", key, start, end, p);
        return web.get().uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }
}