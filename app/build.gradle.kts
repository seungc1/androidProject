// build.gradle.kts (Module:app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Kotlin Kapt 플러그인 추가: Hilt와 Room 어노테이션 프로세서를 위해 필수
    id("org.jetbrains.kotlin.kapt")
    // Hilt 플러그인 적용: 의존성 주입을 위해 필요
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.androidproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // 기존 의존성들
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- 추가된 의존성 시작 ---

    // Kotlin Coroutines (코루틴: 비동기 작업을 효율적으로 처리하기 위한 라이브러리)
    implementation(libs.kotlin.coroutines.core)    // 코루틴 핵심 기능
    implementation(libs.kotlin.coroutines.android) // 안드로이드 환경에서 코루틴 사용 지원

    // Architecture Components (Jetpack ViewModel & LiveData: UI 생명주기 관련 데이터 관리)
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel 사용을 위한 확장 함수
    implementation(libs.androidx.lifecycle.livedata.ktx)  // LiveData 사용을 위한 확장 함수
    implementation(libs.androidx.lifecycle.runtime.ktx)   // LifecycleScope와 같은 런타임 기능 지원

    // Hilt (Dagger Hilt: 의존성 주입 프레임워크. 객체 생성 및 관리를 자동화하여 코드 결합도 낮춤)
    implementation(libs.hilt.android)
    implementation(libs.junit.junit)
    implementation(libs.hilt.android.testing)                        // Hilt 라이브러리 핵심
    kapt(libs.hilt.compiler)                                 // Hilt 어노테이션 프로세서 (컴파일 시 코드 생성)
    implementation(libs.androidx.hilt.navigation.fragment)   // Fragment에서 ViewModel 주입을 돕는 유틸리티 (선택 사항, Navigation Component 사용 시 유용)
    kapt(libs.androidx.hilt.compiler)                        // Hilt Android 컴파일러 (선택 사항, 일부 상황에서 필요할 수 있음)

    // Retrofit (네트워크 통신: RESTful API 요청을 쉽게 만들고 처리)
    implementation(libs.retrofit)               // Retrofit 핵심 라이브러리
    implementation(libs.retrofit.converter.gson) // JSON 데이터를 Kotlin 객체로 변환하기 위한 Gson 컨버터

    // Room Database (로컬 데이터베이스: SQLite를 객체 지향적으로 쉽게 사용)
    implementation(libs.androidx.room.runtime)  // Room 데이터베이스 런타임
    kapt(libs.androidx.room.compiler)           // Room 어노테이션 프로세서 (컴파일 시 DB 관련 코드 생성)
    implementation(libs.androidx.room.ktx)      // Room에서 코루틴을 사용하여 비동기 DB 작업 지원

    // Hilt 테스트 종속성
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler) // "..." 대신 libs. alias 사용 ('hilt-compiler'를 kaptTest 스코프에서 재사용)


    // --- 나머지 테스트 종속성 (JUnit 등) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // For testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Hilt를 사용하기 위해 Kapt가 필요하며, Android Studio 빌드 시스템이 JVM 1.8 이상을 사용해야 합니다.
// (이미 kotlinOptions 블록에 jvmTarget = "1.8"이 설정되어 있습니다.)