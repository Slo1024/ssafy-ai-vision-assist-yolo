package com.project.lookey.product.dto;

import java.util.List;

public record MatchCartResponse(
        int status,
        String message,
        Result result
) {
    public record Result(
            List<String> matched_names
    ) {}
}
