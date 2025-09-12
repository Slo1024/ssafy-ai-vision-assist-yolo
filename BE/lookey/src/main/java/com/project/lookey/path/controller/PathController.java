package com.project.lookey.path.controller;

import com.project.lookey.path.dto.PlaceResponse;
import com.project.lookey.path.service.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/path")
@RequiredArgsConstructor
public class PathController {
    private final PathService service;

    @GetMapping
    public ResponseEntity<PlaceResponse> nearby(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        PlaceResponse result = service.findConvenience(lat, lng);
        return ResponseEntity.ok(result);
    }
}
