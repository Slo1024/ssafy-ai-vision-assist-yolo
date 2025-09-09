package com.project.lookey.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class ApiTestController {

    @Value("${spring.profiles.active:default}")
    private String environment;

    /**
     * Health Check 엔드포인트 - Jenkins에서 배포 확인용
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check 요청 수신");
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "environment", environment,
            "timestamp", LocalDateTime.now().toString(),
            "message", "Lookey API Server is running!"
        ));
    }

    /**
     * 환경 정보 확인용 엔드포인트
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.info("환경 정보 조회 요청");
        return ResponseEntity.ok(Map.of(
            "application", "Lookey Backend API",
            "environment", environment,
            "version", "1.0.0",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * 간단한 Echo 테스트 - API 연결 확인용
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestParam(defaultValue = "Hello Lookey!") String message) {
        log.info("Echo 테스트 요청: {}", message);
        return ResponseEntity.ok(Map.of(
            "echo", message,
            "environment", environment,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * POST 테스트 엔드포인트
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echoPost(@RequestBody Map<String, Object> requestBody) {
        log.info("POST Echo 테스트 요청: {}", requestBody);
        return ResponseEntity.ok(Map.of(
            "received", requestBody,
            "environment", environment,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}