package com.project.lookey.product.controller;

import com.project.lookey.cart.service.CartService;
import com.project.lookey.common.dto.ApiResponse;
import com.project.lookey.product.dto.MatchCartResponse;
import com.project.lookey.product.dto.ProductDirectionResponse;
import com.project.lookey.product.service.AiSearchService;
import com.project.lookey.product.service.PyonyCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
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

    @PostMapping(value = "/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> searchShelf(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestPart("shelf_images") List<MultipartFile> shelfImages
    ) {
        try {
            // 이미지 4장 검증
            if (shelfImages == null || shelfImages.size() != 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "정확히 4장의 이미지가 필요합니다. 현재: " + (shelfImages != null ? shelfImages.size() : "null") + "개");
            }

            // 이미지 파일 형식 검증
            for (int i = 0; i < shelfImages.size(); i++) {
                MultipartFile image = shelfImages.get(i);
                if (image.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "빈 파일이 포함되어 있습니다. 이미지 " + (i+1) + "번째");
                }
                String contentType = image.getContentType();
                if (contentType == null || !contentType.equals("image/jpeg")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "JPEG 파일만 허용됩니다. 이미지 " + (i+1) + "번째 파일형식: " + contentType);
                }
            }

            // 사용자 장바구니 상품명 목록 조회
            List<String> cartProductNames = cartService.getCartProductNames(userId);

            // List를 배열로 변환하여 AI 서비스 호출
            MultipartFile[] imageArray = shelfImages.toArray(new MultipartFile[0]);
            List<String> matchedNames = aiSearchService.findMatchedProducts(imageArray, cartProductNames);

            // 응답 생성
            MatchCartResponse.Result result = new MatchCartResponse.Result(matchedNames.size(), matchedNames);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "매대에서 장바구니 상품 확인 완료",
                    "result", result
            ));

        } catch (ResponseStatusException e) {
            // 이미 적절한 에러 메시지가 있는 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            // 예상치 못한 에러의 경우 상세 정보 포함
            String detailedError = "서버 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage() +
                                  " (userId: " + userId + ", 이미지: " + (shelfImages != null ? shelfImages.size() : "null") + "개)";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, detailedError);
        }
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
        if (contentType == null || !contentType.equals("image/jpeg")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPEG 파일만 허용됩니다.");
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