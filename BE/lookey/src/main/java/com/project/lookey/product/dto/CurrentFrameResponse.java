package com.project.lookey.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CurrentFrameResponse(
        @JsonProperty("multiple")
        boolean multiple,

        @JsonProperty("items")
        List<String> items
) {
}