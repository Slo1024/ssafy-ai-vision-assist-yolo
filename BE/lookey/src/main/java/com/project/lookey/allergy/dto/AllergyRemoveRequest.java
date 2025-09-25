package com.project.lookey.allergy.dto;

import jakarta.validation.constraints.NotNull;

public record AllergyRemoveRequest(
    @NotNull(message = "알레르기 ID는 필수입니다.")
    Long allergyId
) {
}