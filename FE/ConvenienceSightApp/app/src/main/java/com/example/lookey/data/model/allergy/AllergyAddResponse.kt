// data/model/allergy/AllergyAddResponse.kt
package com.example.lookey.data.model.allergy

data class AllergyAddResponse(
    val status: Int,
    val message: String,
    val result: String? // 항상 null
)
data class AllergyPostRequest(
    val request: Request
) {
    data class Request(
        val allergy_id: Long
    )
}
