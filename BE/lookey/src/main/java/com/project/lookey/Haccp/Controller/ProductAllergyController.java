package com.project.lookey.Haccp.Controller;

import com.project.lookey.Haccp.Service.ProductAllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-allergy")
@RequiredArgsConstructor
public class ProductAllergyController {

    private final ProductAllergyService productAllergyService;

    @PostMapping("/update")
    public ResponseEntity<String> updateProductAllergies(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "100") int numOfRows
    ) {
        try {
            productAllergyService.updateProductAllergies(pageNo, numOfRows);
            return ResponseEntity.ok("ProductAllergy 업데이트 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("업데이트 실패: " + e.getMessage());
        }
    }
}
