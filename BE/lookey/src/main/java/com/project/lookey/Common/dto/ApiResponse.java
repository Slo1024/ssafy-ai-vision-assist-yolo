package com.project.lookey.common.dto;

public record ApiResponse<T>(
        Integer status,
        String message,
        T result
) {}