package com.project.lookey.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record CartRemoveRequest(
        @NotNull
        @JsonProperty("cart_id") Integer cartId
) {}
