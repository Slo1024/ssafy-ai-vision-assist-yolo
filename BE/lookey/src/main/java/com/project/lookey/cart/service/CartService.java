package com.project.lookey.cart.service;

import com.project.lookey.OAuth.Repository.UserRepository;
import com.project.lookey.cart.dto.CartAddRequest;
import com.project.lookey.cart.dto.CartListResponse;
import com.project.lookey.cart.dto.CartRemoveRequest;
import com.project.lookey.cart.dto.ProductSearchResponse;
import com.project.lookey.cart.entity.Cart;
import com.project.lookey.cart.repository.CartRepository;
import com.project.lookey.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartListResponse getMyCart(Integer userId) {
        var rows = cartRepository.findRowsByUserId(userId);
        var items = rows.stream()
                .map(r -> new CartListResponse.Item(r.getCartId(), r.getProductId(), r.getProductName()))
                .collect(Collectors.toList());
        return new CartListResponse(items);
    }

    public ProductSearchResponse searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) return new ProductSearchResponse(java.util.List.of());
        var items = productRepository.findNamesByKeyword(keyword.trim())
                .stream()
                .map(v -> new ProductSearchResponse.Item(v.getId(), v.getName()))
                .toList();
        return new ProductSearchResponse(items);
    }

    @Transactional
    public void addItem(Integer userId, CartAddRequest req) {
        Long productId = req.productId();

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        var userRef = userRepository.getReferenceById(userId);

        if (cartRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 장바구니에 담긴 상품입니다.");
        }

        try {
            cartRepository.save(Cart.builder().user(userRef).product(product).build());
        } catch (DataIntegrityViolationException e) {
            // UNIQUE(user_id, product_id) 충돌(경쟁 조건)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 장바구니에 담긴 상품입니다.");
        }
    }

    @Transactional
    public void removeItem(Integer userId, CartRemoveRequest req) {
        int affected = cartRepository.deleteByIdAndUserId(req.cartId(), userId);
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다.");
        }
    }
}
