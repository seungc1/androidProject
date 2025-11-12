// build.gradle.kts (Project: androidProject)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android.gradle.plugin) apply false

    // (★ 추가 ★) 'Safe Args' 플러그인을 '등록'합니다.
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
}