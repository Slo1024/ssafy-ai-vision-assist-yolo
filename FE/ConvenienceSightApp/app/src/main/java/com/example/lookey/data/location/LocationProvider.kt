// data/location/FusedLocationProvider.kt
package com.example.lookey.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.lookey.domain.location.LocationProvider
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class FusedLocationProvider(private val context: Context) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return client.lastLocation.await()
    }
}
