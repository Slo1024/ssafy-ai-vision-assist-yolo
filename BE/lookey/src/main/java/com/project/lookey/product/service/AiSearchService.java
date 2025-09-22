package com.project.lookey.product.service;

import com.project.lookey.product.dto.CurrentFrameResponse;
import com.project.lookey.product.dto.ProductDirectionResponse;
import com.project.lookey.product.dto.ShelfData;
import com.project.lookey.product.dto.ShelfDetectionResponse;
import com.project.lookey.product.dto.ShelfItem;
import com.project.lookey.product.entity.Product;
import com.project.lookey.product.entity.ProductAllergy;
import com.project.lookey.product.repository.ProductRepository;
import com.project.lookey.product.repository.ProductAllergyRepository;
import com.project.lookey.allergy.repository.AllergyRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSearchService {

    private final WebClient webClient;
    private final ProductRepository productRepository;
    private final ProductAllergyRepository productAllergyRepository;
    private final AllergyRepository allergyRepository;
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

            String requestUrl = aiServerUrl + "/api/v1/product/search/ai";
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

            // AI 응답의 모든 상품 상세 로깅
            log.info("🤖 AI 서버 전체 응답 상세:");
            log.info("  - 전체 감지된 상품 개수: {}", response.items().size());
            for (int i = 0; i < response.items().size(); i++) {
                ShelfItem item = response.items().get(i);
                log.info("  - 상품 #{}: 이름='{}', x={}, y={}, w={}, h={}",
                    i+1, item.name(), item.x(), item.y(), item.w(), item.h());
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

    public ProductDirectionResponse.Result findProductDirection(MultipartFile currentFrame, String productName, Integer userId) {
        try {
            // 1단계: Redis에서 저장된 매대 데이터 조회
            ShelfData shelfData = shelfDataService.getShelfData(userId);

            // 2단계: AI 서버에서 현재 화면의 상품들 감지
            CurrentFrameResponse currentFrameResponse = callLocationAI(currentFrame);

            // 3단계: 매대 데이터와 현재 화면 비교하여 위치 계산
            ProductDirectionResponse.Result result = calculateLocationResult(shelfData, currentFrameResponse, productName, userId);

            log.info("상품 위치 안내 완료 - userId: {}, 상품: {}, 결과: {}", userId, productName, result.caseType());

            return result;

        } catch (ResponseStatusException e) {
            // 이미 적절한 에러 메시지가 있는 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("상품 위치 안내 중 예상치 못한 오류 - userId: {}, 상품: {}", userId, productName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "상품 위치 안내 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Optional<Product> findProductByName(String productName) {
        log.debug("상품 검색 시작 - 입력: '{}'", productName);

        // 먼저 정확한 상품명으로 조회
        Optional<Product> exactMatch = productRepository.findByName(productName);
        if (exactMatch.isPresent()) {
            log.debug("정확한 매칭 성공 - 상품: {}", exactMatch.get().getName());
            return exactMatch;
        }

        // 정확한 매칭이 없으면 부분 매칭으로 조회
        List<ProductRepository.NameView> products = productRepository.findNamesByKeyword(productName);
        log.debug("부분 매칭 결과 - 개수: {}", products.size());

        if (!products.isEmpty()) {
            // 첫 번째 매칭 상품의 ID로 전체 정보 조회
            ProductRepository.NameView nameView = products.get(0);
            log.debug("부분 매칭된 상품 - ID: {}, 이름: {}", nameView.getId(), nameView.getName());
            return productRepository.findById(nameView.getId());
        }

        log.debug("상품을 찾을 수 없음 - 입력: '{}'", productName);
        return Optional.empty();
    }

    /**
     * 상품에 대한 사용자의 알레르기 여부 체크
     * @param product 상품 엔티티
     * @param userId 사용자 ID
     * @return 알레르기가 있으면 true, 없으면 false
     */
    private boolean checkUserAllergy(Product product, Integer userId) {
        try {
            // 1. 해당 상품이 포함하는 알레르기 목록 조회
            List<ProductAllergy> productAllergies = productAllergyRepository.findByProduct(product);

            if (productAllergies.isEmpty()) {
                log.debug("상품에 알레르기 정보 없음 - 상품: {}", product.getName());
                return false;
            }

            // 2. 상품의 각 알레르기에 대해 사용자가 해당 알레르기를 가지고 있는지 확인
            for (ProductAllergy productAllergy : productAllergies) {
                Long allergyListId = productAllergy.getAllergy().getId();
                boolean userHasAllergy = allergyRepository.existsByUser_IdAndAllergyList_Id(userId, allergyListId);

                if (userHasAllergy) {
                    log.info("사용자 알레르기 감지 - 상품: '{}', 알레르기: '{}', 사용자: {}",
                            product.getName(), productAllergy.getAllergy().getName(), userId);
                    return true;
                }
            }

            log.debug("사용자 알레르기 없음 - 상품: '{}', 사용자: {}", product.getName(), userId);
            return false;

        } catch (Exception e) {
            log.error("알레르기 체크 중 오류 - 상품: '{}', 사용자: {}", product.getName(), userId, e);
            // 오류 발생 시 안전을 위해 false 반환 (알레르기 없음으로 처리)
            return false;
        }
    }

    /**
     * AI 서버에서 현재 화면의 상품들 감지
     */
    private CurrentFrameResponse callLocationAI(MultipartFile currentFrame) {
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

            String requestUrl = aiServerUrl + "/api/v1/product/search/location/ai";
            CurrentFrameResponse response = webClient
                    .post()
                    .uri(requestUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(CurrentFrameResponse.class)
                    .block();

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버에서 응답을 받지 못했습니다.");
            }

            log.info("AI 서버 전체 응답 - multiple: {}, items: {}, items 개수: {}",
                    response.multiple(), response.items(), response.items() != null ? response.items().size() : 0);

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
     * 개별 경계 범위를 이용한 방향 계산
     */
    private String calculateDirectionWithBoundaries(ShelfItem targetProduct, ShelfItem currentProduct) {
        // 타겟 상품의 경계 범위 계산
        int targetLeft = targetProduct.x() - targetProduct.w() / 2;
        int targetRight = targetProduct.x() + targetProduct.w() / 2;
        int targetTop = targetProduct.y() - targetProduct.h() / 2;
        int targetBottom = targetProduct.y() + targetProduct.h() / 2;

        // 현재 상품의 경계 범위 계산
        int currentLeft = currentProduct.x() - currentProduct.w() / 2;
        int currentRight = currentProduct.x() + currentProduct.w() / 2;
        int currentTop = currentProduct.y() - currentProduct.h() / 2;
        int currentBottom = currentProduct.y() + currentProduct.h() / 2;

        // 겹치는 범위 확인
        boolean horizontalOverlap = !(targetRight < currentLeft || targetLeft > currentRight);
        boolean verticalOverlap = !(targetBottom < currentTop || targetTop > currentBottom);

        // 방향 계산
        String horizontal = "";
        String vertical = "";

        // X축 방향 판단 (겹치지 않을 때만)
        if (!horizontalOverlap) {
            if (targetLeft > currentRight) {
                horizontal = "오른쪽";
            } else if (targetRight < currentLeft) {
                horizontal = "왼쪽";
            }
        }

        // Y축 방향 판단 (겹치지 않을 때만)
        if (!verticalOverlap) {
            if (targetTop > currentBottom) {
                vertical = "위";
            } else if (targetBottom < currentTop) {
                vertical = "아래";
            }
        }

        // 최종 방향 결정
        if (horizontal.isEmpty() && vertical.isEmpty()) {
            return "가운데"; // 겹치는 위치
        } else if (horizontal.isEmpty()) {
            return vertical; // "위" 또는 "아래"
        } else if (vertical.isEmpty()) {
            return horizontal; // "왼쪽" 또는 "오른쪽"
        } else {
            return horizontal + vertical; // "왼쪽위", "오른쪽아래" 등
        }
    }

    /**
     * 매대 데이터와 현재 화면을 비교하여 위치 계산
     */
    private ProductDirectionResponse.Result calculateLocationResult(ShelfData shelfData, CurrentFrameResponse currentFrame, String productName, Integer userId) {
        if (shelfData == null || shelfData.items() == null || shelfData.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 매대 정보가 없습니다. 먼저 매대를 스캔해주세요.");
        }

        // AI 응답 로그 추가
        log.info("AI 응답 분석 - multiple: {}, items: {}, 찾는 상품: {}",
                currentFrame.multiple(), currentFrame.items(), productName);

        // 매대에서 타겟 상품 찾기
        ShelfItem targetProduct = shelfData.items().stream()
                .filter(item -> isProductNameMatch(item.name(), productName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 상품이 매대에서 발견되지 않았습니다."));

        // 현재 화면에 상품이 여러 개 감지된 경우 (multiple: true)
        if (currentFrame.multiple()) {
            log.info("🔄 MULTIPLE=TRUE 경로 진입 - 다중 상품 감지 모드 - 감지된 상품들: {}", currentFrame.items());

            // 현재 화면의 상품들을 매대 데이터와 매칭
            Optional<ShelfItem> currentProductOpt = shelfData.items().stream()
                    .filter(shelfItem -> currentFrame.items().stream()
                            .anyMatch(currentItem -> isProductNameMatch(shelfItem.name(), currentItem)))
                    .findFirst();

            if (currentProductOpt.isPresent()) {
                ShelfItem currentProduct = currentProductOpt.get();
                log.info("매대에서 현재 위치 상품 찾음 - 현재: '{}', 목표: '{}'",
                        currentProduct.name(), productName);

                String direction = calculateDirectionWithBoundaries(targetProduct, currentProduct);
                log.info("다중 상품 감지 - 방향 안내 반환: {}", direction);

                ProductDirectionResponse.Target target = new ProductDirectionResponse.Target(productName, direction);
                return new ProductDirectionResponse.Result("DIRECTION", target, null);
            } else {
                log.error("다중 상품 감지 - 매대에서 현재 화면 상품을 찾을 수 없음: {}", currentFrame.items());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 화면의 상품들을 매대에서 찾을 수 없습니다.");
            }
        } else {
            // 현재 화면에 상품이 1개만 감지된 경우 (multiple: false)
            log.info("⭕ MULTIPLE=FALSE 경로 진입 - 단일 상품 모드");
            if (currentFrame.items().size() == 1) {
                String detectedProduct = currentFrame.items().get(0);
                log.info("단일 상품 감지 - AI 감지: '{}', FE 요청: '{}'", detectedProduct, productName);

                // AI가 감지한 상품명과 FE에서 요청한 상품명이 같은지 확인
                if (isProductNameMatch(detectedProduct, productName)) {
                    // 상품명이 같은 경우: SINGLE_RECOGNIZED + DB에서 상품 정보 조회
                    log.info("상품명 매칭 성공 - DB에서 상품 정보 조회 시작: {}", productName);
                    Optional<Product> productOpt = findProductByName(productName);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        log.info("상품 DB 조회 성공 - 이름: {}, 가격: {}, 이벤트: {}",
                                product.getName(), product.getPrice(), product.getEvent());

                        // 사용자 알레르기 체크
                        boolean hasAllergy = checkUserAllergy(product, userId);

                        ProductDirectionResponse.Info info = new ProductDirectionResponse.Info(
                                product.getName(),
                                product.getPrice(),
                                product.getEvent(),
                                hasAllergy
                        );
                        return new ProductDirectionResponse.Result("SINGLE_RECOGNIZED", null, info);
                    } else {
                        log.warn("상품 DB 조회 실패 - 상품명: {}", productName);
                        // DB에서 찾지 못한 경우도 SINGLE_RECOGNIZED로 반환 (알레르기 정보 없음)
                        ProductDirectionResponse.Info info = new ProductDirectionResponse.Info(
                                productName,
                                null,
                                null,
                                false
                        );
                        return new ProductDirectionResponse.Result("SINGLE_RECOGNIZED", null, info);
                    }
                } else {
                    // 상품명이 다른 경우: DIRECTION + 매대 데이터 기반 방향 안내
                    log.info("상품명 다름 - 방향 안내 모드로 전환");

                    // 매대 데이터에서 AI가 감지한 상품 찾기
                    Optional<ShelfItem> currentProductOpt = shelfData.items().stream()
                            .filter(item -> isProductNameMatch(item.name(), detectedProduct))
                            .findFirst();

                    if (currentProductOpt.isPresent()) {
                        ShelfItem currentProduct = currentProductOpt.get();
                        String direction = calculateDirectionWithBoundaries(targetProduct, currentProduct);
                        log.info("방향 계산 완료 - 현재: '{}', 목표: '{}', 방향: '{}'",
                                detectedProduct, productName, direction);

                        ProductDirectionResponse.Target target = new ProductDirectionResponse.Target(productName, direction);
                        return new ProductDirectionResponse.Result("DIRECTION", target, null);
                    } else {
                        log.error("매대 데이터에서 AI 감지 상품을 찾을 수 없음 - 감지된 상품: '{}'", detectedProduct);
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 화면의 상품을 매대에서 찾을 수 없습니다.");
                    }
                }
            } else {
                log.error("현재 화면에서 상품 감지 실패 - 감지된 상품 개수: {}", currentFrame.items().size());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 화면에서 상품을 감지할 수 없습니다.");
            }
        }
    }
}