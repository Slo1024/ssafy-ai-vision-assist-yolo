package com.example.lookey.data.remote.dto

import com.example.lookey.data.model.allergy.*
import com.example.lookey.data.network.ApiService
import com.example.lookey.domain.entity.Allergy
import com.example.lookey.domain.repo.AllergyRepository
import retrofit2.Response


class AllergyRepositoryImpl(
    private val api: ApiService
) : AllergyRepository {

    private fun <T> ensureSuccess(res: Response<T>): T {
        if (!res.isSuccessful) {
            val errorBody = try {
                res.errorBody()?.string()
            } catch (_: Exception) {
                null
            }
            throw IllegalStateException(
                "HTTP ${res.code()} ${res.message()} ${errorBody ?: ""}".trim()
            )
        }
        return res.body() ?: throw IllegalStateException("Empty body")
    }


    override suspend fun list(): List<Allergy> {
        val res = api.getAllergies()
        val body = ensureSuccess(res)

        if (body.status !in 200..299) throw IllegalStateException(body.message)
        return body.result?.names.orEmpty().map { Allergy(it.allergy_id, it.name) }
    }

    override suspend fun search(q: String): List<Allergy> {
    val keyword = q.trim()
    if (keyword.isEmpty()) return emptyList() // 빈 문자열이면 요청하지 않음
    val res = api.searchAllergies(keyword)
    val body = ensureSuccess(res)

    if (body.status !in 200..299) throw IllegalStateException(body.message)
    return body.result?.items.orEmpty().map { Allergy(it.id, it.name) }
    }

    override suspend fun add(allergyId: Int) {
        val res = api.addAllergy(
            AllergyPostRequest(
                request = AllergyPostRequest.Request(allergy_id = allergyId)
            )
        )
        val body = ensureSuccess(res)

        if (body.status !in 200..299) throw IllegalStateException(body.message)
        // result: 항상 null → 처리 없음
    }

    override suspend fun delete(allergyId: Int) {
        val res = api.deleteAllergy(AllergyDeleteRequest(allergyId))
        val body = ensureSuccess(res)

        if (body.status !in 200..299) throw IllegalStateException(body.message)
    }
}
