// data/model/allergy/AllergyGetResponse.kt
package com.example.lookey.data.model.allergy

data class AllergyNameDto(
    val allergyId: Long,
    val allergyListId: Long,
    val allergyName: String
)

data class AllergyListResult(
    val items: List<AllergyNameDto>
)

data class AllergyGetResponse(
    val status: Int,
    val message: String,
    val result: AllergyListResult?
)
