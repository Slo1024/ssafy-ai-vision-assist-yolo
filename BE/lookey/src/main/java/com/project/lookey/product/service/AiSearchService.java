package com.project.lookey.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.search.url:http://localhost:8000}")
    private String aiServerUrl;

    public List<String> findMatchedProducts(MultipartFile[] images, List<String> cartProductNames) {
        try {
            String url = aiServerUrl + "/api/product/search/ai";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 이미지 4장 추가
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                    @Override
                    public String getFilename() {
                        return image.getOriginalFilename();
                    }
                };
                body.add("shelf_images", resource);
            }

            // 장바구니 상품명 목록을 JSON 문자열로 변환하여 추가
            String cartProductNamesJson = objectMapper.writeValueAsString(cartProductNames);
            body.add("cart_product_names", cartProductNamesJson);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    requestEntity, 
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                @SuppressWarnings("unchecked")
                List<String> matchedNames = (List<String>) responseBody.get("matched_names");
                return matchedNames != null ? matchedNames : List.of();
            } else {
                log.error("AI 서버 응답 오류: {}", response.getStatusCode());
                return List.of();
            }

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류", e);
            return List.of();
        } catch (IOException e) {
            log.error("이미지 파일 읽기 오류", e);
            return List.of();
        } catch (Exception e) {
            log.error("AI 서버 통신 오류", e);
            return List.of();
        }
    }
}