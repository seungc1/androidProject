// build.gradle.kts (Module:app)
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

// local.properties 파일에서 API 키를 읽어오기 위한 설정
val properties = Properties()
try {
    properties.load(project.rootProject.file("local.properties").inputStream())
} catch (e: Exception) {
    project.logger.warn("local.properties file not found. API keys will be missing.")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.androidproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val gptApiKey = properties.getProperty("GPT_API_KEY")?.trim('"') ?: ""
        buildConfigField("String", "GPT_API_KEY", "\"$gptApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    // --- 1. AndroidX Core & UI ---
    // (★ 수정 ★) 'core-ktx'에서 '충돌' '라이브러리' '제외'
    implementation(libs.androidx.core.ktx) {
        exclude(group = "androidx.legacy", module = "legacy-support-v4")
    }
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    // --- 2. Architecture & Coroutines ---
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- 3. 의존성 주입 (Hilt) ---
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)

    // --- 4. 네트워킹 (Retrofit) ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // --- 5. 로컬 데이터베이스 (Room) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // --- 6. '달력' '및' '날짜' '라이브러리' ---
    // (★ 수정 ★) '달력' '라이브러리'에서 '충돌' '라이브러리' '제외'
    implementation(libs.androidx.material.calendarview) {
        exclude(group = "androidx.legacy", module = "legacy-support-v4")
    }
    implementation(libs.androidx.threetenabp) // (★ 추가 ★) 'threeten' '설치'

    // --- 7. 유닛 테스트 ---
    testImplementation(libs.junit)

    // --- 8. 안드로이드 UI 테스트 ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
}