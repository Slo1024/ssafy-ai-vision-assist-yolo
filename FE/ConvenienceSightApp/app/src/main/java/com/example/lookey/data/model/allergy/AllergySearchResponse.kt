package com.example.lookey.data.model.allergy

data class AllergySearchResponse(
    val status: Int,
    val message: String,
    val result: AllergySearchResult?
)

data class AllergySearchResult(
    val items: List<AllergySearchItem> = emptyList()
)

data class AllergySearchItem(
    val id: Long,
    val name: String
)

