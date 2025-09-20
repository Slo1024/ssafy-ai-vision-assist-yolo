package com.example.lookey.domain.location

import android.location.Location

interface LocationProvider {
    suspend fun getCurrentLocation(): Location?
}
