package com.example.lookey.data.network

import com.example.lookey.network.dto.PlaceItemDto

class PathRepository(private val api: ApiService) {
    suspend fun nearby(lat: Double, lng: Double): List<PlaceItemDto> {
        return api.getNearbyStores(lat, lng).result?.items ?: emptyList()
    }
}
