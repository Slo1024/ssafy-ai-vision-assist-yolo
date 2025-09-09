package com.project.lookey.controller;

import com.project.lookey.service.FoodNutritionService;
import com.project.lookey.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class ApiTestController {

    private final FoodNutritionService foodNutritionService;
    private final LocationService locationService;

    @Value("${app.environment}")
    private String environment;

    @Value("${app.debug}")
    private boolean debugEnabled;

    /**
     * 환경 정보 확인용 엔드포인트
     */
    @GetMapping("/environment")
    public Map<String, Object> getEnvironmentInfo() {
        return Map.of(
            "environment", environment,
            "debug", debugEnabled,
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 식품 영양 정보 조회 테스트
     * @param foodName 식품명
     * @return 영양 정보
     */
    @GetMapping("/nutrition")
    public Mono<String> testFoodNutrition(@RequestParam String foodName) {
        log.info("식품 영양 정보 테스트 요청: {}", foodName);
        return foodNutritionService.getFoodNutritionInfo(foodName);
    }

    /**
     * 농축수산물 정보 조회 테스트
     * @param productName 상품명
     * @return 상품 정보
     */
    @GetMapping("/agriculture")
    public Mono<String> testAgricultureProduct(@RequestParam String productName) {
        log.info("농축수산물 정보 테스트 요청: {}", productName);
        return foodNutritionService.getAgricultureProductInfo(productName);
    }

    /**
     * 주변 매장 검색 테스트 (Google Maps)
     * @param lat 위도
     * @param lng 경도
     * @param radius 반경 (미터)
     * @param keyword 검색 키워드
     * @return 매장 정보
     */
    @GetMapping("/stores/google")
    public Mono<String> testGoogleMapsSearch(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "convenience store") String keyword) {
        log.info("Google Maps 매장 검색 테스트: 위치({}, {}), 반경: {}m", lat, lng, radius);
        return locationService.searchNearbyStores(lat, lng, radius, keyword);
    }

    /**
     * 주변 매장 검색 테스트 (네이버 지도)
     * @param query 검색어
     * @param coordinate 좌표 (위도,경도)
     * @return 매장 정보
     */
    @GetMapping("/stores/naver")
    public Mono<String> testNaverMapSearch(
            @RequestParam String query,
            @RequestParam String coordinate) {
        log.info("네이버 지도 매장 검색 테스트: 검색어({}), 좌표({})", query, coordinate);
        return locationService.searchStoresWithNaver(query, coordinate);
    }

    /**
     * Health Check 엔드포인트
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
            "status", "UP",
            "environment", environment,
            "timestamp", System.currentTimeMillis()
        );
    }
}