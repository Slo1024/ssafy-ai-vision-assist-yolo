package com.project.lookey.allergy.controller;

import com.project.lookey.allergy.dto.AllergyAddRequest;
import com.project.lookey.allergy.dto.AllergyListResponse;
import com.project.lookey.allergy.dto.AllergyRemoveRequest;
import com.project.lookey.allergy.dto.AllergySearchResponse;
import com.project.lookey.allergy.service.AllergyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/allergy")
@RequiredArgsConstructor
@Tag(name = "Allergy", description = "알레르기 관련 API")
public class AllergyController {

    private final AllergyService allergyService;

    @GetMapping
    public ResponseEntity<?> list(
            @AuthenticationPrincipal(expression = "id") Integer userId
    ) {
        AllergyListResponse data = allergyService.getMyAllergies(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "알레르기 목록 조회 성공",
                "result", data
        ));
    }

    @GetMapping("/search/{searchword}")
    public ResponseEntity<?> search(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @PathVariable("searchword") String searchword
    ) {
        AllergySearchResponse data = allergyService.searchAllergies(searchword);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "알레르기 검색 성공",
                "result", data
        ));
    }

    @PostMapping
    public ResponseEntity<?> add(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @Valid @RequestBody AllergyAddRequest request
    ) {
        allergyService.addAllergy(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", 201,
                "message", "알레르기를 등록했습니다.",
                "result", null
        ));
    }

    @DeleteMapping(consumes = "application/json")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @Valid @RequestBody AllergyRemoveRequest request
    ) {
        allergyService.removeAllergy(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "알레르기를 삭제했습니다.",
                "result", null
        ));
    }
}