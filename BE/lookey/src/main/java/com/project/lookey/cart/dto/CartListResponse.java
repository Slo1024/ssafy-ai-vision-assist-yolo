package com.project.lookey.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CartListResponse(
        List<Item> items
) {
    public record Item(
            @JsonProperty("cart_id") Integer cartId,
            @JsonProperty("product_id") Long productId,
            @JsonProperty("product_name") String productName
    ) {}
}