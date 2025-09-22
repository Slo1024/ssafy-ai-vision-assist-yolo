// app/src/main/java/com/example/lookey/ConveniSightApp.kt
package com.example.lookey

import android.app.Application
import android.content.Context

object AppCtx {
    lateinit var app: Context
        private set

    fun init(context: Context) {
        app = context
    }
}

class ConveniSightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.init(applicationContext)   // ✅ 여기서 설치
    }
}
