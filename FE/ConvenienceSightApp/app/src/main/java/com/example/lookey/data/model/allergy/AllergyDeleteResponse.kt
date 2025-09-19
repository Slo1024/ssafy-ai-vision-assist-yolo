package com.example.lookey.data.model.allergy

data class AllergyDeleteRequest(
    val allergy_id: Int
)

data class AllergyDeleteResponse(
    val status: Int,
    val message: String,
    val result: String?
)