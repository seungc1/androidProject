// app/src/main/java/com/example/androidproject/RehabAIApp.kt
package com.example.androidproject

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlin.text.Typography.dagger

@HiltAndroidApp // Hilt가 이 Application 클래스를 사용하여 의존성 그래프를 생성하도록 지시합니다.
class RehabAIApp : Application() {
    // 앱이 시작될 때 필요한 초기화 작업들을 여기에 추가할 수 있습니다.
    override fun onCreate() {
        super.onCreate()
        // 예를 들어, Timber (로깅 라이브러리) 초기화 등
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(Timber.DebugTree())
        // }
    }
}


