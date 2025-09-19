package com.example.lookey.data.settings

import android.content.Context
import com.example.lookey.data.network.RetrofitClient
import com.example.lookey.data.remote.dto.AllergyRepositoryImpl
import com.example.lookey.domain.repo.AllergyRepository
import com.example.lookey.ui.viewmodel.AllergyViewModel

object ServiceLocator {
    fun allergyViewModel(context: Context): AllergyViewModel {
        val api = RetrofitClient.apiService
        val repo: AllergyRepository = AllergyRepositoryImpl(api)
        return AllergyViewModel(repo)
    }
}
