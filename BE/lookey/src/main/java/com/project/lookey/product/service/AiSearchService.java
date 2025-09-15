package com.project.lookey.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.lookey.product.dto.ProductDirectionResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    @Value("${ai.search.url:http://localhost:8000}")
    private String aiServerUrl;

    public List<String> findMatchedProducts(MultipartFile[] images, List<String> cartProductNames) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // 이미지 4장 추가
            for (MultipartFile image : images) {
                ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                    @Override
                    public String getFilename() {
                        return image.getOriginalFilename();
                    }
                };
                builder.part("shelf_images", resource);
            }

            // 장바구니 상품명 목록을 JSON 문자열로 변환하여 추가
            String cartProductNamesJson = objectMapper.writeValueAsString(cartProductNames);
            builder.part("cart_product_names", cartProductNamesJson);

            Map<String, Object> response = webClient
                    .post()
                    .uri(aiServerUrl + "/api/product/search/ai")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                @SuppressWarnings("unchecked")
                List<String> matchedNames = (List<String>) response.get("matched_names");
                return matchedNames != null ? matchedNames : List.of();
            } else {
                log.warn("AI 서버에서 빈 응답을 받았습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에서 응답을 받지 못했습니다.");
            }

        } catch (WebClientResponseException e) {
            log.error("AI 서버 HTTP 오류 - 상태코드: {}, 메시지: {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().is5xxServerError()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에 일시적인 문제가 발생했습니다.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI 서버 요청이 올바르지 않습니다.");
            }
        } catch (JsonProcessingException e) {
            log.error("장바구니 상품명 JSON 변환 오류", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "요청 데이터 처리 중 오류가 발생했습니다.");
        } catch (IOException e) {
            log.error("이미지 파일 읽기 오류", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽을 수 없습니다.");
        } catch (Exception e) {
            log.error("AI 서버 통신 중 예상치 못한 오류", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서비스 처리 중 오류가 발생했습니다.");
        }
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
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                String caseType = (String) response.get("case");
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