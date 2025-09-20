package com.example.lookey.ui.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lookey.data.network.PathRepository
import com.example.lookey.domain.location.LocationProvider
import com.example.lookey.ui.storemap.StoreFinderViewModel

class StoreFinderViewModelFactory(
    private val repo: PathRepository,
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreFinderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreFinderViewModel(repo, locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
