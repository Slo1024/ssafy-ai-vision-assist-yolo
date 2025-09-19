package com.project.lookey.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ShelfDetectionResponse(
        @JsonProperty("items")
        List<ShelfItem> items
) {
}