package com.project.lookey.vision.controller;

import com.project.lookey.vision.service.VisionApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/vision/ai")
@RequiredArgsConstructor
@Tag(name = "Vision Analysis API", description = "Google Cloud Vision API를 이용한 이미지 분석")
public class VisionAnalysisController {

    private final VisionApiService visionApiService;

    @PostMapping("/analyze")
    @Operation(
            summary = "이미지 분석",
            description = "업로드된 이미지를 Google Cloud Vision API로 분석하여 사람, 장애물, 카운터, 방향, 선반, 카테고리 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "분석 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일이 없거나 올바르지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public Mono<ResponseEntity<Map<String, Object>>> analyzeImage(
            @Parameter(description = "분석할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Vision API 이미지 분석 요청 - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        return Mono.fromCallable(() -> {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
            }

            // 파일 크기 제한 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
            }

            // 이미지 파일 타입 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            return file.getBytes();
        })
        .flatMap(visionApiService::analyzeImage)
        .map(this::createSuccessResponse)
        .onErrorResume(this::createErrorResponse);
    }

    

    private ResponseEntity<Map<String, Object>> createSuccessResponse(Map<String, Object> analysisResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이미지 분석이 완료되었습니다.");
        response.put("data", analysisResult);
        response.put("timestamp", System.currentTimeMillis());

        log.info("Vision API 분석 성공 - 카테고리: {}", analysisResult.get("category"));
        return ResponseEntity.ok(response);
    }

    private Mono<ResponseEntity<Map<String, Object>>> createErrorResponse(Throwable error) {
        log.error("Vision API 분석 실패", error);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "이미지 분석 중 오류가 발생했습니다.");
        errorResponse.put("error", error.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        int statusCode = 500;
        if (error instanceof IllegalArgumentException) {
            statusCode = 400;
        }

        return Mono.just(ResponseEntity.status(statusCode).body(errorResponse));
    }
}