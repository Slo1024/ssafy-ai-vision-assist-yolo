package com.example.lookey.domain.entity


data class Allergy(
    val id: Long,           // Allergy 테이블 ID
    val allergyListId: Long, // AllergyList 테이블 ID (추가/삭제 시 사용)
    val name: String
)
