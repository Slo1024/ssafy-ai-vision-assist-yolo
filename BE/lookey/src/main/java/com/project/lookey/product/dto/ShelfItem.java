package com.project.lookey.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShelfItem(
        @JsonProperty("name")
        String name,

        @JsonProperty("x")
        int x,

        @JsonProperty("y")
        int y,

        @JsonProperty("w")
        int w,

        @JsonProperty("h")
        int h
) {
}