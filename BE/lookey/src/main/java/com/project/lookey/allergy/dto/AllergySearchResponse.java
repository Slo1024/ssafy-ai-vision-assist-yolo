package com.project.lookey.allergy.dto;

import java.util.List;

public record AllergySearchResponse(
    List<Item> items
) {
    public record Item(
        Long id,
        String name
    ) {
    }
}