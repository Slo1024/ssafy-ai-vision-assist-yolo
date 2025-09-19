package com.project.lookey.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.lookey.product.dto.ProductDirectionResponse;
import com.project.lookey.product.dto.ShelfDetectionResponse;
import com.project.lookey.product.dto.ShelfItem;
import com.project.lookey.product.entity.Product;
import com.project.lookey.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSearchService {

    private final WebClient webClient;
    private final ProductRepository productRepository;
    private final ShelfDataService shelfDataService;

    @Value("${ai.search.url}")
    private String aiServerUrl;

    public List<String> findMatchedProducts(MultipartFile[] images, List<String> cartProductNames, Integer userId) {
        try {
            // 1단계: AI 서버에서 매대 전체 상품 감지
            ShelfDetectionResponse shelfResponse = detectShelfProducts(images);

            // 2단계: Redis에 매대 데이터 저장
            shelfDataService.saveShelfData(userId, shelfResponse);

            // 3단계: 장바구니 상품과 매칭
            List<String> matchedNames = matchProductsWithCart(shelfResponse.items(), cartProductNames);

            log.info("매대 상품 매칭 완료 - userId: {}, 전체 상품: {}개, 매칭된 상품: {}개",
                    userId, shelfResponse.items().size(), matchedNames.size());

            return matchedNames;

        } catch (Exception e) {
            log.error("매대 상품 검색 중 오류 발생 - userId: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "매대 상품 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * AI 서버에서 매대 전체 상품 감지
     */
    private ShelfDetectionResponse detectShelfProducts(MultipartFile[] images) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // 이미지 1장 추가 (API 문서에 따라 1장으로 변경)
            for (MultipartFile image : images) {
                ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                    @Override
                    public String getFilename() {
                        return image.getOriginalFilename();
                    }
                };
                builder.part("shelf_images", resource);
            }

            String requestUrl = aiServerUrl + "/api/product/search/ai";
            ShelfDetectionResponse response = webClient
                    .post()
                    .uri(requestUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(ShelfDetectionResponse.class)
                    .block();

            if (response == null || response.items() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에서 올바른 응답을 받지 못했습니다.");
            }

            return response;

        } catch (WebClientResponseException e) {
            String errorDetails = "AI 서버 오류 (상태코드: " + e.getStatusCode() + ")";
            if (e.getStatusCode().is5xxServerError()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, errorDetails + " - AI 서버에 일시적인 문제가 발생했습니다.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorDetails + " - AI 서버 요청이 올바르지 않습니다.");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서비스 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 매대 상품과 장바구니 상품 매칭
     */
    private List<String> matchProductsWithCart(List<ShelfItem> shelfItems, List<String> cartProductNames) {
        return shelfItems.stream()
                .map(ShelfItem::name)
                .filter(shelfProductName ->
                    cartProductNames.stream()
                            .anyMatch(cartProductName ->
                                    isProductNameMatch(shelfProductName, cartProductName)
                            )
                )
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 상품명 매칭 로직 (정확한 매칭 + 부분 매칭)
     */
    private boolean isProductNameMatch(String shelfProductName, String cartProductName) {
        if (shelfProductName == null || cartProductName == null) {
            return false;
        }

        // 정확한 매칭
        if (shelfProductName.equals(cartProductName)) {
            return true;
        }

        // 대소문자 무시 매칭
        if (shelfProductName.equalsIgnoreCase(cartProductName)) {
            return true;
        }

        // 부분 매칭 (공백 제거 후)
        String normalizedShelf = shelfProductName.replaceAll("\\s+", "");
        String normalizedCart = cartProductName.replaceAll("\\s+", "");

        return normalizedShelf.contains(normalizedCart) || normalizedCart.contains(normalizedShelf);
    }

    public ProductDirectionResponse.Result findProductDirection(MultipartFile currentFrame, String productName) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // 현재 화면 이미지 추가
            ByteArrayResource resource = new ByteArrayResource(currentFrame.getBytes()) {
                @Override
                public String getFilename() {
                    return currentFrame.getOriginalFilename();
                }
            };
            builder.part("current_frame", resource);

            // 상품명 추가
            builder.part("product_name", productName);

            Map<String, Object> response = webClient
                    .post()
                    .uri(aiServerUrl + "/api/product/search/location/ai")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                @SuppressWarnings("unchecked")
                String caseType = (String) response.get("case");
                @SuppressWarnings("unchecked")
                String output = (String) response.get("output");

                if ("NotFound".equals(caseType)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 화면에서 해당 상품을 찾을 수 없습니다.");
                }

                if ("DIRECTION".equals(caseType)) {
                    ProductDirectionResponse.Target target = new ProductDirectionResponse.Target(productName, output);
                    return new ProductDirectionResponse.Result(caseType, target, null);
                } else if ("SINGLE_RECOGNIZED".equals(caseType)) {
                    // 상품 정보 조회
                    Optional<Product> productOpt = findProductByName(output);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        ProductDirectionResponse.Info info = new ProductDirectionResponse.Info(
                                product.getName(),
                                product.getPrice(),
                                product.getEvent(),
                                false // allergy 정보는 현재 Product 엔티티에 없으므로 기본값
                        );
                        return new ProductDirectionResponse.Result(caseType, null, info);
                    } else {
                        // 상품 정보를 찾을 수 없는 경우 기본 정보 반환
                        ProductDirectionResponse.Info info = new ProductDirectionResponse.Info(
                                output,
                                null,
                                null,
                                false
                        );
                        return new ProductDirectionResponse.Result(caseType, null, info);
                    }
                } else {
                    log.warn("알 수 없는 case type: {}", caseType);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버 응답 형식이 올바르지 않습니다.");
                }
            } else {
                log.warn("AI 서버에서 빈 응답을 받았습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에서 응답을 받지 못했습니다.");
            }

        } catch (WebClientResponseException e) {
            log.error("AI 서버 HTTP 오류 - 상태코드: {}, 메시지: {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 화면에서 해당 상품을 찾을 수 없습니다.");
            } else if (e.getStatusCode().is5xxServerError()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에 일시적인 문제가 발생했습니다.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI 서버 요청이 올바르지 않습니다.");
            }
        } catch (IOException e) {
            log.error("이미지 파일 읽기 오류", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽을 수 없습니다.");
        } catch (Exception e) {
            log.error("AI 서버 통신 중 예상치 못한 오류", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서비스 처리 중 오류가 발생했습니다.");
        }
    }

    private Optional<Product> findProductByName(String productName) {
        // 먼저 정확한 상품명으로 조회
        Optional<Product> exactMatch = productRepository.findByName(productName);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // 정확한 매칭이 없으면 부분 매칭으로 조회
        List<ProductRepository.NameView> products = productRepository.findNamesByKeyword(productName);
        if (!products.isEmpty()) {
            // 첫 번째 매칭 상품의 ID로 전체 정보 조회
            return productRepository.findById(products.get(0).getId());
        }

        return Optional.empty();
    }
}