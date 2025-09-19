package com.project.lookey.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record ShelfData(
        @JsonProperty("items")
        List<ShelfItem> items,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("user_id")
        Integer userId
) {
    public static ShelfData from(ShelfDetectionResponse response, Integer userId) {
        return new ShelfData(
                response.items(),
                LocalDateTime.now(),
                userId
        );
    }
}