// build.gradle.kts (Module:app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Kotlin Kapt 플러그인 추가: Hilt와 Room 어노테이션 프로세서를 위해 필수
    id("org.jetbrains.kotlin.kapt")
    // Hilt 플러그인 적용: 의존성 주입을 위해 필요
    id("com.google.dagger.hilt.android")
}
kotlin {
    jvmToolchain(17) // (성준민 수정) '11' -> '17' 버전으로 '올려줘' 요청 반영
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 34

    // --- 여기부터 추가 ---
    buildFeatures {
        viewBinding = true
    }
    // --- 여기까지 추가 ---

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
        sourceCompatibility = JavaVersion.VERSION_17 // (성준민 수정) '11' -> '17' 버전으로 '올려줘' 요청 반영
        targetCompatibility = JavaVersion.VERSION_17 // (성준민 수정) '11' -> '17' 버전으로 '올려줘' 요청 반영
    }

    /* (수정 1) '설정 충돌'을 해결하기 위해 '이전' 방법인 'kotlinOptions'를 주석 처리합니다.
       (jvmToolchain(17)이 이 역할을 대신합니다.)
    kotlinOptions {
        jvmTarget = "11"
    }
    */
}

dependencies {

    // --- 기본 의존성 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- (추가) AndroidX Navigation ---
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- (수정 2) 'RecyclerView' '부품 없음' 오류를 해결하기 위해 '추가'합니다.
    implementation(libs.androidx.recyclerview)

    // --- Kotlin Coroutines ---
    implementation(libs.kotlin.coroutines.core)    // 코루틴 핵심 기능
    implementation(libs.kotlin.coroutines.android) // 안드로이드 환경에서 코루틴 사용 지원

    // --- Architecture Components (ViewModel & LiveData) ---
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel 사용을 위한 확장 함수
    implementation(libs.androidx.lifecycle.livedata.ktx)  // LiveData 사용을 위한 확장 함수
    implementation(libs.androidx.lifecycle.runtime.ktx)   // LifecycleScope와 같은 F런타임 기능 지원

    // --- Hilt (Dagger Hilt) ---
    implementation(libs.hilt.android) // Hilt 라이브러리 핵심
    kapt(libs.hilt.compiler)          // Hilt 어노테이션 프로세서 (컴파일 시 코드 생성)
    implementation(libs.androidx.hilt.navigation.fragment) // Fragment에서 ViewModel 주입 (선택 사항)
    kapt(libs.androidx.hilt.compiler) // Hilt Android 컴파일러 (선택 사항)

    // --- Retrofit (네트워크 통신) ---
    implementation(libs.retrofit)               // Retrofit 핵심 라이브러리
    implementation(libs.retrofit.converter.gson) // JSON 데이터를 Kotlin 객체로 변환하기 위한 Gson 컨버터

    // --- Room Database (로컬 데이터베이스) ---
    implementation(libs.androidx.room.runtime)  // Room 데이터베이스 런타임
    kapt(libs.androidx.room.compiler)           // Room 어노테이션 프로세서 (컴파일 시 DB 관련 코드 생성)
    implementation(libs.androidx.room.ktx)      // Room에서 코루틴을 사용하여 비동기 DB 작업 지원

    // --- Hilt 테스트 종속성 (중복 제거 및 정리) ---
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler) // 'hilt-compiler'를 kaptTest 스코프에서 재사용

    // --- 나머지 테스트 종속성 (중복 제거 및 정리) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}