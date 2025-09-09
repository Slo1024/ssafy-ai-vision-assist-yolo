package com.project.lookey.service;

import com.project.lookey.config.ExternalApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final ExternalApiConfig externalApiConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * Google Maps API를 통해 주변 매장을 검색합니다.
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 검색 반경 (미터)
     * @param keyword 검색 키워드 (예: "convenience store")
     * @return 매장 정보
     */
    public Mono<String> searchNearbyStores(double latitude, double longitude, int radius, String keyword) {
        log.info("주변 매장 검색 요청: 위치({}, {}), 반경: {}m, 키워드: {}", latitude, longitude, radius, keyword);
        
        WebClient webClient = webClientBuilder
                .baseUrl(externalApiConfig.getGoogleMaps().getUrl())
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/nearbysearch/json")
                        .queryParam("location", latitude + "," + longitude)
                        .queryParam("radius", radius)
                        .queryParam("keyword", keyword)
                        .queryParam("key", externalApiConfig.getGoogleMaps().getKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("Google Maps API 응답 수신"))
                .doOnError(error -> log.error("Google Maps API 호출 실패: ", error));
    }

    /**
     * 네이버 지도 API를 통해 주변 매장을 검색합니다. (대안)
     * @param query 검색어
     * @param coordinate 좌표 (위도,경도)
     * @return 매장 정보
     */
    public Mono<String> searchStoresWithNaver(String query, String coordinate) {
        log.info("네이버 지도 검색 요청: 검색어({}), 좌표({})", query, coordinate);
        
        WebClient webClient = webClientBuilder
                .baseUrl(externalApiConfig.getNaverMap().getUrl())
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", externalApiConfig.getNaverMap().getClientId())
                .defaultHeader("X-NCP-APIGW-API-KEY", externalApiConfig.getNaverMap().getClientSecret())
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/map-place/v1/search")
                        .queryParam("query", query)
                        .queryParam("coordinate", coordinate)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("네이버 지도 API 응답 수신"))
                .doOnError(error -> log.error("네이버 지도 API 호출 실패: ", error));
    }
}