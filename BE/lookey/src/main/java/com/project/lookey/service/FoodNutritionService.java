package com.project.lookey.service;

import com.project.lookey.config.ExternalApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodNutritionService {

    private final ExternalApiConfig externalApiConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * 식품의약품안전처 API를 통해 식품 영양 정보를 조회합니다.
     * @param foodName 식품명
     * @return 영양 정보
     */
    public Mono<String> getFoodNutritionInfo(String foodName) {
        log.info("식품 영양 정보 조회 요청: {}", foodName);
        
        WebClient webClient = webClientBuilder
                .baseUrl(externalApiConfig.getFoodSafety().getUrl())
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getNtrItdntList1")
                        .queryParam("serviceKey", externalApiConfig.getFoodSafety().getKey())
                        .queryParam("type", "json")
                        .queryParam("numOfRows", "10")
                        .queryParam("pageNo", "1")
                        .queryParam("FOOD_NM_KR", foodName)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("API 응답 수신: {}", response.substring(0, Math.min(200, response.length()))))
                .doOnError(error -> log.error("API 호출 실패: ", error));
    }

    /**
     * 농촌진흥청 API를 통해 농축수산물 정보를 조회합니다.
     * @param productName 상품명
     * @return 상품 정보
     */
    public Mono<String> getAgricultureProductInfo(String productName) {
        log.info("농축수산물 정보 조회 요청: {}", productName);
        
        WebClient webClient = webClientBuilder
                .baseUrl(externalApiConfig.getRda().getUrl())
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getProductInfo")
                        .queryParam("serviceKey", externalApiConfig.getRda().getKey())
                        .queryParam("type", "json")
                        .queryParam("numOfRows", "10")
                        .queryParam("pageNo", "1")
                        .queryParam("productName", productName)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("RDA API 응답 수신: {}", response.substring(0, Math.min(200, response.length()))))
                .doOnError(error -> log.error("RDA API 호출 실패: ", error));
    }
}