// build.gradle.kts (Module:app)
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)

    // Kotlin Kapt: Hilt, Room 등 어노테이션 프로세서 실행을 위해 필요
    id("org.jetbrains.kotlin.kapt")
    // Hilt: 의존성 주입을 위해 필요
    id("com.google.dagger.hilt.android")
}

// local.properties 파일에서 API 키를 읽어오기 위한 설정
val properties = Properties()
try {
    properties.load(project.rootProject.file("local.properties").inputStream())
} catch (e: Exception) {
    // local.properties 파일이 없어도 빌드는 되도록 예외 처리
    project.logger.warn("local.properties file not found. API keys will be missing.")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 34

    buildFeatures {
        // View Binding 활성화 (XML 레이아웃 바인딩 클래스 자동 생성)
        viewBinding = true
        // buildConfigField를 사용하기 위해 buildConfig 활성화
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.androidproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties에서 읽어온 API 키를 BuildConfig에 추가
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview) // 목록(RecyclerView)
    implementation(libs.androidx.fragment.ktx)  // Fragment KTX (by viewModels() 등)
    implementation(libs.androidx.swiperefreshlayout)

    // --- 2. Architecture & Coroutines ---
    // Navigation (Fragment 간 이동)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Coroutines (비동기 처리)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)
    // Lifecycle (ViewModel, LiveData, LifecycleScope)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- 3. 의존성 주입 (Hilt) ---
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment) // Navigation 컴포넌트와 Hilt 통합

    // --- 4. 네트워킹 (Retrofit) ---
    implementation(libs.retrofit)                // Retrofit 본체
    implementation(libs.retrofit.converter.gson) // Gson 변환기

    // --- 5. 로컬 데이터베이스 (Room) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // 코루틴 지원
    kapt(libs.androidx.room.compiler)

    // --- 6. 유닛 테스트 ---
    testImplementation(libs.junit)

    // --- 7. 안드로이드 UI 테스트 ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Hilt 테스트 지원
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
}