package com.project.lookey.allergy.dto;

import java.util.List;

public record AllergySearchResponse(
    List<AllergySearchItem> items
) {
}