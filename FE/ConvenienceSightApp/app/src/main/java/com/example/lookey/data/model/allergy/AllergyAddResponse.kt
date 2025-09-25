// data/model/allergy/AllergyAddResponse.kt
package com.example.lookey.data.model.allergy

data class AllergyAddResponse(
    val status: Int,
    val message: String,
    val result: Any? // Object나 String 모두 허용
)
