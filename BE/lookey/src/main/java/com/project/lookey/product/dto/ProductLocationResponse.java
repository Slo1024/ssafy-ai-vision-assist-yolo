package com.project.lookey.product.dto;

public record ProductLocationResponse(
        int status,
        String message,
        Result result
) {
    public record Result(
            String direction_bucket
    ) {}
}