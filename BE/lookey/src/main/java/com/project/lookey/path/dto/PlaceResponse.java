package com.project.lookey.path.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlaceResponse {
    private int status;       // HTTP 상태 코드
    private String message;   // 응답 메시지

    private Result result;    // 실제 데이터

    @Data
    @AllArgsConstructor
    public static class Result {
        private List<Item> items;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private String name;      // 편의점 이름
        private String address;   // 편의점 주소
        private Double lat;       // 위도
        private Double lng;       // 경도
        private Integer distance; // 현재 위치와의 거리(m)
        private String brand;     // GS25, CU 등

        @JsonProperty("place_id") // JSON 필드명을 place_id로 맞춤
        private String placeId;   // Kakao place id
    }
}
