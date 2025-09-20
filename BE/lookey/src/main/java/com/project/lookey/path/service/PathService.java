package com.project.lookey.path.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.lookey.path.client.KakaoClient;
import com.project.lookey.path.dto.PlaceResponse;
import com.project.lookey.path.exception.PathException;
import com.project.lookey.path.utils.BrandUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathService {

    private final KakaoClient kakao;

    public PlaceResponse findConvenience(double lat, double lng) {
        int radius = 5000;
        int limit = 3;

        log.info("[PathService] 카카오 API 호출 준비 lat={}, lng={}", lat, lng);


        try {
            JsonNode json = kakao.searchConvenience(lat, lng, radius);

            if (json == null) {
                log.error("카카오 API 응답이 null입니다. lat: {}, lng: {}", lat, lng);
                throw PathException.kakaoApiError("카카오 API 응답이 없습니다");
            }

            JsonNode docs = json.path("documents");

            if (docs.isEmpty() || docs.size() == 0) {
                log.info("검색 결과가 없습니다. lat: {}, lng: {}, radius: {}m", lat, lng, radius);
                throw PathException.noResults(radius);
            }

            List<PlaceResponse.Item> items = new ArrayList<>();

            for (int i = 0; i < docs.size() && items.size() < limit; i++) {
                JsonNode d = docs.get(i);

                try {
                    String name = d.path("place_name").asText("");
                    if (name.isEmpty()) {
                        log.warn("편의점 이름이 비어있습니다. 건너뛰는 중...");
                        continue;
                    }

                    String address = !d.path("road_address_name").asText().isEmpty()
                            ? d.path("road_address_name").asText()
                            : d.path("address_name").asText();

                    Double itemLng = d.path("x").asDouble(); // 경도
                    Double itemLat = d.path("y").asDouble(); // 위도

                    // 좌표값 검증
                    if (itemLat == 0.0 && itemLng == 0.0) {
                        log.warn("잘못된 좌표값입니다. 건너뛰는 중... name: {}", name);
                        continue;
                    }

                    Integer distance = d.has("distance") ? d.get("distance").asInt() : null;
                    String brand = BrandUtil.detect(name);
                    String placeId = d.path("id").asText("");

                    items.add(new PlaceResponse.Item(name, address, itemLat, itemLng, distance, brand, placeId));

                } catch (Exception e) {
                    log.warn("개별 편의점 정보 파싱 오류, 건너뛰는 중: {}", e.getMessage());
                }
            }

            // 파싱 후에도 결과가 없으면
            if (items.isEmpty()) {
                log.info("유효한 편의점 정보가 없습니다. lat: {}, lng: {}", lat, lng);
                throw PathException.noResults(radius);
            }

            log.info("편의점 {}곳 조회 완료. lat: {}, lng: {}", items.size(), lat, lng);
            return new PlaceResponse(
                    200,
                    "가까운 편의점 " + items.size() + "곳 조회 성공",
                    new PlaceResponse.Result(items)
            );

        } catch (PathException e) {
            // PathException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("편의점 검색 중 예상치 못한 오류 발생. lat: {}, lng: {}", lat, lng, e);
            throw PathException.kakaoApiError("편의점 검색 처리 중 오류: " + e.getMessage());
        }
    }
}
