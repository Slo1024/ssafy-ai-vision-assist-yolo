package com.project.lookey.path.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient webClient;

    @Value("${kakao.rest.key}")
    private String restKey;

    public JsonNode searchConvenience(double lat, double lng, int radius) {
        return webClient.get()
                .uri(uri -> uri
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/category.json")
                        .queryParam("category_group_code", "CS2") // 편의점 카테고리
                        .queryParam("y", lat)  // 위도
                        .queryParam("x", lng)  // 경도
                        .queryParam("radius", radius)   // 0~20000
                        .queryParam("sort", "distance") // 거리순
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restKey) // Kakao REST 키
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(); // 단순화를 위해 동기 호출(성공 플로우만)
    }
}
