package com.project.lookey.product.controller;

import com.project.lookey.cart.service.CartService;
import com.project.lookey.common.dto.ApiResponse;
import com.project.lookey.product.dto.MatchCartResponse;
import com.project.lookey.product.dto.ProductDirectionResponse;
import com.project.lookey.product.service.AiSearchService;
import com.project.lookey.product.service.PyonyCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    private final PyonyCrawler crawler;
    private final CartService cartService;
    private final AiSearchService aiSearchService;

    @PostMapping("/seven/drinks")
    public ResponseEntity<Void> run(@RequestParam(defaultValue="1") int start,
                                    @RequestParam(defaultValue="50") int end) throws Exception {
        crawler.crawlDrinks(start, end);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchShelf(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam("shelf_images") MultipartFile[] shelfImages
    ) {
        // 이미지 4장 검증
        if (shelfImages == null || shelfImages.length != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "정확히 4장의 이미지가 필요합니다.");
        }

        // 이미지 파일 형식 검증
        for (MultipartFile image : shelfImages) {
            if (image.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일이 포함되어 있습니다.");
            }
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPG 또는 PNG 파일만 허용됩니다.");
            }
        }

        // 사용자 장바구니 상품명 목록 조회
        List<String> cartProductNames = cartService.getCartProductNames(userId);

        // AI 서비스로 매칭된 상품명 조회
        List<String> matchedNames = aiSearchService.findMatchedProducts(shelfImages, cartProductNames);

        // 응답 생성
        MatchCartResponse.Result result = new MatchCartResponse.Result(matchedNames.size(), matchedNames);
        
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "매대에서 장바구니 상품 확인 완료",
                "result", result
        ));
    }

    @PostMapping("/search/location")
    public ResponseEntity<ApiResponse<ProductDirectionResponse.Result>> findProductDirection(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam("current_frame") MultipartFile currentFrame,
            @RequestParam("product_name") String productName
    ) {
        // 이미지 파일 검증
        if (currentFrame == null || currentFrame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 화면 이미지가 필요합니다.");
        }

        String contentType = currentFrame.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPG 또는 PNG 파일만 허용됩니다.");
        }

        // 상품명 검증
        if (productName == null || productName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품명이 필요합니다.");
        }

        // AI 서비스로 상품 위치 조회
        ProductDirectionResponse.Result result = aiSearchService.findProductDirection(currentFrame, productName.trim());

        // 케이스별 메시지 설정
        String message;
        if ("DIRECTION".equals(result.caseType())) {
            message = "상품 방향 안내 성공";
        } else if ("SINGLE_RECOGNIZED".equals(result.caseType())) {
            message = "단일 상품 인식 완료";
        } else {
            message = "상품 검색 완료";
        }

        return ResponseEntity.ok(new ApiResponse<>(200, message, result));
    }
}