package com.project.lookey.Common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseUtil {

    public static ResponseEntity<Map<String, Object>> ok(String message, Object result) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", message,
                "result", result
        ));
    }

    public static ResponseEntity<Map<String, Object>> created(String message, Object result) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", 201,
                "message", message,
                "result", result
        ));
    }

    public static ResponseEntity<Map<String, Object>> error(int status, String message, String errorDetail) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status,
                "message", message + " (" + errorDetail + ")",
                "result", ""
        ));
    }
}

