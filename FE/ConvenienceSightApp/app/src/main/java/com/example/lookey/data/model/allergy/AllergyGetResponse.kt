// data/model/allergy/AllergyGetResponse.kt
package com.example.lookey.data.model.allergy

data class AllergyNameDto(
    val allergy_id: Int,
    val name: String
)

data class AllergyListResult(
    val names: List<AllergyNameDto>
)

data class AllergyGetResponse(
    val status: Int,
    val message: String,
    val result: AllergyListResult?
)
