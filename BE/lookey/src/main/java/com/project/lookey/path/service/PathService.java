package com.project.lookey.path.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.lookey.path.client.KakaoClient;
import com.project.lookey.path.dto.PlaceResponse;
import com.project.lookey.path.utils.BrandUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PathService {

    private final KakaoClient kakao;

    public PlaceResponse findConvenience(double lat, double lng) {
        int radius = 5000;
        int limit = 3;

        JsonNode json = kakao.searchConvenience(lat, lng, radius);
        JsonNode docs = json.path("documents");

        List<PlaceResponse.Item> items = new ArrayList<>();

        for (int i = 0; i < docs.size() && items.size() < limit; i++) {
            JsonNode d = docs.get(i);

            String name = d.path("place_name").asText("");
            String address = !d.path("road_address_name").asText().isEmpty()
                    ? d.path("road_address_name").asText()
                    : d.path("address_name").asText();

            Double itemLng = d.path("x").asDouble(); // 경도
            Double itemLat = d.path("y").asDouble(); // 위도
            Integer distance = d.has("distance") ? d.get("distance").asInt() : null;
            String brand = BrandUtil.detect(name);
            String placeId = d.path("id").asText("");

            items.add(new PlaceResponse.Item(name, address, itemLat, itemLng, distance, brand, placeId));
        }

        return new PlaceResponse(
                200,
                "가까운 편의점 3곳 조회 성공",
                new PlaceResponse.Result(items)
        );
    }
}
