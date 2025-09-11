package com.project.lookey.cart.controller;

import com.project.lookey.cart.dto.CartAddRequest;
import com.project.lookey.cart.dto.CartListResponse;
import com.project.lookey.cart.dto.CartRemoveRequest;
import com.project.lookey.cart.dto.ProductSearchResponse;
import com.project.lookey.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> list(
            @AuthenticationPrincipal(expression = "id") Integer userId
    ) {
        CartListResponse data = cartService.getMyCart(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "장바구니 목록 조회 성공",
                "result", data
        ));
    }

    @GetMapping("/search/{searchword}")
    public ResponseEntity<?> search(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @PathVariable("searchword") String searchword
    ) {
        ProductSearchResponse data = cartService.searchProducts(searchword);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "검색 성공",
                "result", data
        ));
    }

    @PostMapping
    public ResponseEntity<?> add(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @Valid @RequestBody CartAddRequest request
    ) {
        cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", 201,
                "message", "장바구니에 상품을 담았습니다.",
                "result", null
        ));
    }

    @DeleteMapping(consumes = "application/json")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal(expression = "id") Integer userId,
            @Valid @RequestBody CartRemoveRequest request
    ) {
        cartService.removeItem(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "장바구니에서 삭제하였습니다.",
                "result", null
        ));
    }
}
