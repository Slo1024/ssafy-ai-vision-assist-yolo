package com.project.lookey.path.controller;

import com.project.lookey.path.dto.ErrorResponse;
import com.project.lookey.path.dto.PlaceResponse;
import com.project.lookey.path.exception.PathException;
import com.project.lookey.path.service.PathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/path")
@RequiredArgsConstructor
public class PathController {
    private final PathService service;

    @GetMapping
    public ResponseEntity<?> nearby(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        log.info("[PathController] 요청 수신 lat={}, lng={}", lat, lng);

        try {
            // 필수 파라미터 검증
            if (lat == null) {
                throw PathException.invalidParameter("lat");
            }
            if (lng == null) {
                throw PathException.invalidParameter("lng");
            }

            // 좌표 범위 검증
            if (!isValidCoordinate(lat, lng)) {
                throw PathException.invalidParameter("lat or lng out of valid range");
            }

            PlaceResponse result = service.findConvenience(lat, lng);
            return ResponseEntity.ok(result);

        } catch (PathException e) {
            log.warn("Path API 오류 - {}: {}", e.getCode(), e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                e.getHttpStatus().value(),
                e.getMessage(),
                new ErrorResponse.Error(e.getCode(), e.getInfo())
            );
            return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);

        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            ErrorResponse errorResponse = new ErrorResponse(
                500,
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                new ErrorResponse.Error("INTERNAL_SERVER_ERROR", e.getMessage())
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
