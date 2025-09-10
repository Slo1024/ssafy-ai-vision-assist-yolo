package com.project.lookey.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
@Tag(name = "Common", description = "공통 API")
public class ApiTestController {

    @Value("${spring.profiles.active:default}")
    private String environment;

    @Operation(
        summary = "서버 상태 확인", 
        description = "서버가 정상적으로 실행 중인지 확인하는 Health Check API",
        responses = {
            @ApiResponse(responseCode = "200", description = "서버 정상 동작")
        }
    )
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

    @Operation(
        summary = "서버 환경 정보 조회",
        description = "현재 서버의 환경 정보를 조회합니다"
    )
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

    @Operation(
        summary = "Echo 테스트 (GET)",
        description = "메시지를 그대로 반환하는 API 연결 테스트"
    )
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(
        @Parameter(description = "Echo할 메시지", example = "Hello Lookey!")
        @RequestParam(defaultValue = "Hello Lookey!") String message
    ) {
        log.info("Echo 테스트 요청: {}", message);
        return ResponseEntity.ok(Map.of(
            "echo", message,
            "environment", environment,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @Operation(
        summary = "Echo 테스트 (POST)",
        description = "POST로 전송된 데이터를 그대로 반환하는 테스트"
    )
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echoPost(
        @Parameter(description = "Echo할 JSON 데이터")
        @RequestBody Map<String, Object> requestBody
    ) {
        log.info("POST Echo 테스트 요청: {}", requestBody);
        return ResponseEntity.ok(Map.of(
            "received", requestBody,
            "environment", environment,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}