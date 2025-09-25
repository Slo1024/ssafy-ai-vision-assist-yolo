package com.project.lookey.allergy.dto;

import java.util.List;

public record AllergyListResponse(
    List<Item> items
) {
    public record Item(
        Long allergyId,
        Long allergyListId, 
        String allergyName
    ) {
    }
}