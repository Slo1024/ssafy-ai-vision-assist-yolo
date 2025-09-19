package com.project.lookey.Haccp.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.lookey.Haccp.Config.ApiConfig;
import com.project.lookey.Haccp.Dto.ApiItem;
import com.project.lookey.Haccp.Dto.ApiResponse;
import com.project.lookey.allergy.entity.AllergyList;
import com.project.lookey.allergy.repository.AllergyListRepository;
import com.project.lookey.product.entity.Product;
import com.project.lookey.product.entity.ProductAllergy;
import com.project.lookey.product.repository.ProductAllergyRepository;
import com.project.lookey.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductAllergyService {

    private final ProductRepository productRepository;
    private final AllergyListRepository allergyListRepository;
    private final ProductAllergyRepository productAllergyRepository;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;

    @Transactional
    public void updateProductAllergies(int pageNo, int numOfRows) throws Exception {
        // 1. URL 생성 및 ServiceKey 인코딩
        String serviceKey = URLEncoder.encode(apiConfig.getApiKey(), StandardCharsets.UTF_8);
        String urlStr = apiConfig.getApiUrl()
                + "?ServiceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&returnType=json";

        // 2. HttpURLConnection으로 GET 호출
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("API 호출 실패: HTTP " + responseCode);
        }

        // 3. 응답 읽기
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
        }

        // 4. JSON -> DTO
        ApiResponse apiResponse = objectMapper.readValue(responseBody.toString(), ApiResponse.class);
        if (apiResponse.getBody() == null || apiResponse.getBody().getItems() == null) return;

        // 5. 상품 & 알러지 매칭
        List<Product> products = productRepository.findAll();
        List<AllergyList> allergies = allergyListRepository.findAll();

        System.out.println("===== API 응답 처리 시작 =====");

        for (ApiResponse.Body.ItemWrapper wrapper : apiResponse.getBody().getItems()) {
            ApiItem apiItem = wrapper.getItem();
            if (apiItem == null) continue;

            System.out.printf("API 상품명=%s, API 알러지=%s%n",
                    apiItem.getPrdlstNm(), apiItem.getAllergy());

            String productNameFromApi = normalize(apiItem.getPrdlstNm());
            String allergyStr = apiItem.getAllergy();

            if (productNameFromApi.isBlank() || allergyStr == null
                    || allergyStr.isBlank() || allergyStr.equals("없음")) continue;

            String[] apiAllergies = allergyStr.split(",");

            // DB 상품 순회
            for (Product product : products) {
                String dbProductName = normalize(product.getName());

                // 상품명 유연 매칭
                if (dbProductName.contains(productNameFromApi) || productNameFromApi.contains(dbProductName)) {

                    // API 알러지 각각 확인
                    for (String apiAllergy : apiAllergies) {
                        String normalizedApiAllergy = normalize(apiAllergy);

                        for (AllergyList allergy : allergies) {
                            String dbAllergyName = normalize(allergy.getName());

                            if (dbAllergyName.contains(normalizedApiAllergy) || normalizedApiAllergy.contains(dbAllergyName)) {
                                boolean exists = productAllergyRepository.existsByProductAndAllergy(product, allergy);

                                if (!exists) {
                                    ProductAllergy pa = new ProductAllergy();
                                    pa.setProduct(product);
                                    pa.setAllergy(allergy);
                                    productAllergyRepository.save(pa);

                                    System.out.printf("[추가됨] 상품=%s, 알러지=%s%n",
                                            product.getName(), allergy.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 문자열 정규화 메서드
     * - 공백 제거
     * - 특수문자 제거
     * - 소문자 변환
     */
    private String normalize(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s+", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "")
                .toLowerCase();
    }
}
