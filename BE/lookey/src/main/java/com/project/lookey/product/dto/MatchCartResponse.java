package com.project.lookey.product.dto;

import java.util.List;

public record MatchCartResponse(
        int status,
        String message,
        Result result
) {
    public record Result(
            int count,
            List<String> matched_names
    ) {}
}
