package com.project.lookey.path.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Error error;

    @Data
    @AllArgsConstructor
    public static class Error {
        private String code;
        private String info;
    }
}