package com.example.lookey

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lookey.data.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * 임시 테스트 액티비티 - 다른 API들의 인증을 테스트합니다.
 * MainActivity 대신 이것을 실행해보세요.
 */
class TestAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            testAllAPIs()
        }
    }

    private suspend fun testAllAPIs() {
        val api = RetrofitClient.apiService

        Log.d("TestAuth", "========================================")
        Log.d("TestAuth", "Starting API Authentication Tests")
        Log.d("TestAuth", "========================================")

        // 1. 알레르기 API 테스트 (GET)
        try {
            Log.d("TestAuth", "Testing: GET /api/v1/allergy")
            val response = api.getAllergies()
            Log.d("TestAuth", "Allergy API Response: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("TestAuth", "✅ SUCCESS - Token works for Allergy API")
                Log.d("TestAuth", "Response body: ${response.body()}")
            } else {
                Log.e("TestAuth", "❌ FAILED - ${response.code()}: ${response.message()}")
                Log.e("TestAuth", "Error body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TestAuth", "Exception in Allergy API", e)
        }

        // 2. 장바구니 API 테스트 (GET)
        try {
            Log.d("TestAuth", "Testing: GET /api/v1/carts")
            val response = api.getCartList()
            Log.d("TestAuth", "Cart API Response: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("TestAuth", "✅ SUCCESS - Token works for Cart API")
                Log.d("TestAuth", "Response body: ${response.body()}")
            } else {
                Log.e("TestAuth", "❌ FAILED - ${response.code()}: ${response.message()}")
                Log.e("TestAuth", "Error body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TestAuth", "Exception in Cart API", e)
        }

        // 3. 상품 검색 API 테스트 (POST with search keyword)
        try {
            Log.d("TestAuth", "Testing: GET /api/v1/carts/search/테스트")
            val response = api.searchProducts("테스트")
            Log.d("TestAuth", "Product Search API Response: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("TestAuth", "✅ SUCCESS - Token works for Product Search API")
                Log.d("TestAuth", "Response body: ${response.body()}")
            } else {
                Log.e("TestAuth", "❌ FAILED - ${response.code()}: ${response.message()}")
                Log.e("TestAuth", "Error body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TestAuth", "Exception in Product Search API", e)
        }

        Log.d("TestAuth", "========================================")
        Log.d("TestAuth", "API Authentication Tests Completed")
        Log.d("TestAuth", "========================================")
    }
}