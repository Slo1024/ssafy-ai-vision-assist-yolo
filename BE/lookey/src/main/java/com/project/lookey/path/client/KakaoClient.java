package com.project.lookey.path.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.lookey.path.exception.PathException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient webClient;

    @Value("${kakao.rest.key}")
    private String restKey;

    public JsonNode searchConvenience(double lat, double lng, int radius) {
        try {
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
                    .onStatus(status -> status.is4xxClientError(), response -> {
                        log.error("카카오 API 클라이언트 오류: {} - lat: {}, lng: {}", response.statusCode(), lat, lng);
                        if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                            return Mono.error(PathException.kakaoApiError("카카오 API 인증 실패 - API 키를 확인하세요"));
                        } else if (response.statusCode() == HttpStatus.BAD_REQUEST) {
                            return Mono.error(PathException.kakaoApiError("카카오 API 요청 파라미터 오류"));
                        } else if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            return Mono.error(PathException.rateLimitExceeded());
                        } else {
                            return Mono.error(PathException.kakaoApiError("카카오 API 클라이언트 오류: " + response.statusCode()));
                        }
                    })
                    .onStatus(status -> status.is5xxServerError(), response -> {
                        log.error("카카오 API 서버 오류: {} - lat: {}, lng: {}", response.statusCode(), lat, lng);
                        return Mono.error(PathException.kakaoApiError("카카오 서버 일시적 오류"));
                    })
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10)) // 10초 타임아웃
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)) // 최대 2회 재시도, 500ms 간격
                            .filter(throwable -> !(throwable instanceof PathException))) // PathException은 재시도하지 않음
                    .block();

        } catch (WebClientResponseException e) {
            log.error("카카오 API HTTP 오류 - 상태코드: {}, 메시지: {}, lat: {}, lng: {}",
                    e.getStatusCode(), e.getMessage(), lat, lng);

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw PathException.kakaoApiError("카카오 API 인증 실패 - API 키를 확인하세요");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw PathException.rateLimitExceeded();
            } else if (e.getStatusCode().is5xxServerError()) {
                throw PathException.kakaoApiError("카카오 서버 일시적 오류");
            } else {
                throw PathException.kakaoApiError("카카오 API 오류: " + e.getStatusCode());
            }

        } catch (Exception e) {
            log.error("카카오 API 예상치 못한 오류 - lat: {}, lng: {}", lat, lng, e);

            if (e instanceof PathException) {
                throw e; // PathException은 그대로 전파
            }

            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                throw PathException.kakaoApiError("카카오 API 서버에 연결할 수 없습니다");
            } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw PathException.kakaoApiError("카카오 API 응답 지연 - 잠시 후 다시 시도하세요");
            } else {
                throw PathException.kakaoApiError("카카오 API 통신 중 오류 발생: " + e.getMessage());
            }
        }
    }
}
