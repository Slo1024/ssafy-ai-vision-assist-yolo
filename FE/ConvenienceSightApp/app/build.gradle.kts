// app/build.gradle.kts
import java.util.Properties
import java.io.FileInputStream
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
//    alias(libs.plugins.hilt.android)
//    id("org.jetbrains.kotlin.kapt")    // ← 버전 없이 'id'로 적용 (중요)
}

android {
    namespace = "com.example.lookey"
    compileSdk = 35

    defaultConfig {

        // 1) local.properties에서 읽기
        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) FileInputStream(f).use { load(it) }
        }

        // 2) 값이 없으면 환경변수로 대체(선택)
        val apiBaseUrlRaw = props.getProperty("API_BASE_URL")
            ?: System.getenv("API_BASE_URL")

        check(!apiBaseUrlRaw.isNullOrBlank()) {
            "Missing API_BASE_URL. Put it in local.properties or export as environment variable."
        }

        // 3) 끝에 슬래시 보장
        val apiBaseUrl = if (apiBaseUrlRaw!!.endsWith("/")) apiBaseUrlRaw else "$apiBaseUrlRaw/"

        // 4) BuildConfig에 주입
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")


        applicationId = "com.example.lookey"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }



    buildTypes {
        debug {
            buildConfigField("boolean", "USE_AUTH", "true")          // 로그인부터 시작
            buildConfigField("boolean", "SHOW_LOGIN_SKIP", "true")   // 건너뛰기 버튼 표시
        }
        release {
            buildConfigField("boolean", "USE_AUTH", "true")          // 로그인부터 시작
            buildConfigField("boolean", "SHOW_LOGIN_SKIP", "false")  // 배포용: 건너뛰기 숨김
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }


    // JDK 17 권장
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }


    buildFeatures {
        compose = true
        buildConfig = true   // BuildConfig 활성화
    }
}


dependencies {



    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // 기본 AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.play.services.auth)


    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)


    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // CameraX
    implementation(libs.androidx.camera.core)        // -> implementation(libs.androidx.camera.core)  (자동완성에서 'androidx-camera-core')
    implementation(libs.androidx.camera.camera2)     // -> implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)   // -> implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)        // -> implementation(libs.androidx.camera.view)



// Location / Maps
    implementation(libs.play.services.location)      // -> implementation(libs.play.services.location)  (자동완성에서 'play-services-location')
    implementation(libs.play.services.maps)          // -> implementation(libs.play.services.maps)      (자동완성에서 'play-services-maps')
    implementation(libs.maps.compose)                // 그대로

// Image / Network
    implementation(libs.coil.compose)                // -> implementation(libs.coil.compose) (자동완성에서 'coil-compose')
    implementation(libs.retrofit)                    // 그대로
    implementation(libs.okhttp.logging)              // -> implementation(libs.okhttp.logging) (자동완성에서 'okhttp-logging')
    implementation(libs.kotlinx.serialization.json)  // -> implementation(libs.kotlinx.serialization.json) ('kotlinx-serialization-json')
// DataStore (설정값 저장용)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
// Hilt
//    implementation(libs.hilt.android)                // -> implementation(libs.hilt.android) ('hilt-android')
//    kapt(libs.hilt.compiler)                         // -> kapt(libs.hilt.compiler) ('hilt-compiler')


    // 백엔드 연동
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")


    // 테스트
    testImplementation(libs.junit)

    // Android Instrumentation Test
    androidTestImplementation("androidx.test:core:1.5.0")           // ApplicationProvider
    androidTestImplementation("androidx.test.ext:junit:1.1.5")      // AndroidJUnit4
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose UI 테스트
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

}
