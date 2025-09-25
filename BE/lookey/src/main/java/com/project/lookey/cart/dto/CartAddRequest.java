package com.project.lookey.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
        @NotNull
        @JsonProperty("product_id")
        Long productId
) {}
