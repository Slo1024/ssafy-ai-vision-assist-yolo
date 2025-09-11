package com.project.lookey.product.controller;

import com.project.lookey.product.service.PyonyCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawl")
public class ProductController {
    private final PyonyCrawler crawler;

    @PostMapping("/seven/drinks")
    public ResponseEntity<Void> run(@RequestParam(defaultValue="1") int start,
                                    @RequestParam(defaultValue="50") int end) throws Exception {
        crawler.crawlDrinks(start, end);
        return ResponseEntity.ok().build();
    }
}