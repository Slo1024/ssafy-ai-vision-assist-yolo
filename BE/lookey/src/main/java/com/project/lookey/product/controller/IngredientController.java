package com.project.lookey.product.controller;

import com.project.lookey.product.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService service;

    @PostMapping("/import/seven")
    public ResponseEntity<String> importSeven() throws Exception {
        int n = service.importForSevenDrinks();
        return ResponseEntity.ok("imported products: " + n);
    }
}
