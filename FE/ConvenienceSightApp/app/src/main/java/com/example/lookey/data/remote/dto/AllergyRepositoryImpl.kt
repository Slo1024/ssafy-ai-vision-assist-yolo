package com.example.lookey.data.remote.dto

import android.content.Context
import android.util.Log
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.data.model.allergy.*
import com.example.lookey.data.network.ApiService
import com.example.lookey.domain.entity.Allergy
import com.example.lookey.domain.repo.AllergyRepository
import com.example.lookey.util.PrefUtil


class AllergyRepositoryImpl(
    private val api: ApiService,
    private val context: Context
) : AllergyRepository {

    private fun ensureToken() {
        if (TokenProvider.token == null) {
            val savedToken = PrefUtil.getJwtToken(context)
            if (!savedToken.isNullOrEmpty()) {
                TokenProvider.token = savedToken
                Log.d("AllergyRepo", "Token loaded from preferences: ${savedToken.take(20)}...")
            } else {
                Log.e("AllergyRepo", "No token found in preferences!")
            }
        }
    }

    override suspend fun list(): List<Allergy> {
        ensureToken()
        Log.d("AllergyRepo", "Current token: ${TokenProvider.token?.take(20)}...")
        val response = api.getAllergies()
        Log.d("AllergyRepo", "List response: $response")

        return if (response.isSuccessful) {
            val body = response.body()
            Log.d("AllergyRepo", "List body: $body")
            body?.result?.items?.map {
                Allergy(it.allergyId, it.allergyListId, it.allergyName)
            } ?: emptyList()
        } else {
            Log.e("AllergyRepo", "getAllergies failed: ${response.code()}")
            emptyList()
        }
    }

    override suspend fun search(q: String): List<Allergy> {
        val keyword = q.trim()
        if (keyword.isEmpty()) return emptyList()

        ensureToken()
        val response = api.searchAllergies(keyword)
        Log.d("AllergyRepo", "Search response: $response")

        return if (response.isSuccessful) {
            val body = response.body()
            Log.d("AllergyRepo", "Search body: $body")
            body?.result?.items?.map {
                Allergy(it.id, it.id, it.name) // 검색에서는 id가 allergyListId
            } ?: emptyList()
        } else {
            Log.e("AllergyRepo", "searchAllergies failed: ${response.code()}")
            emptyList()
        }
    }

    override suspend fun add(allergyId: Long) {
        // 파라미터 allergyId는 실제로는 allergyListId여야 함
        ensureToken()

        val request = AllergyPostRequest(allergyId = allergyId)
        Log.d("AllergyRepo", "Adding allergyListId: $allergyId")
        Log.d("AllergyRepo", "Request JSON: ${com.google.gson.Gson().toJson(request)}")
        val response = api.addAllergy(request)
        Log.d("AllergyRepo", "Add response: ${response.code()} - ${response.message()}")
        val body = response.body()
        Log.d("AllergyRepo", "Add body: $body")

        if (!response.isSuccessful) {
            Log.e("AllergyRepo", "addAllergy failed: ${response.code()}")
            throw RuntimeException("알러지 추가 실패: ${response.code()}")
        } else {
            Log.d("AllergyRepo", "Add successful!")
        }
    }

    override suspend fun delete(allergyId: Long) {
        // 파라미터 allergyId는 실제로는 allergyListId여야 함
        ensureToken()

        val request = AllergyDeleteRequest(allergyId)
        Log.d("AllergyRepo", "Deleting allergyListId: $allergyId")
        Log.d("AllergyRepo", "Delete JSON: ${com.google.gson.Gson().toJson(request)}")
        val response = api.deleteAllergy(request)
        Log.d("AllergyRepo", "Delete response: ${response.code()} - ${response.message()}")
        val body = response.body()
        Log.d("AllergyRepo", "Delete body: $body")

        if (!response.isSuccessful) {
            Log.e("AllergyRepo", "deleteAllergy failed: ${response.code()}")
            throw RuntimeException("알러지 삭제 실패: ${response.code()}")
        } else {
            Log.d("AllergyRepo", "Delete successful!")
        }
    }
}
