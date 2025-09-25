package com.example.lookey.domain.repo

import com.example.lookey.domain.entity.Allergy

interface AllergyRepository {
    suspend fun list(): List<Allergy>
    suspend fun search(q: String): List<Allergy>
    suspend fun add(allergyId: Long)
    suspend fun delete(allergyId: Long)
}