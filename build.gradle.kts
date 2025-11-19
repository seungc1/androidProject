// build.gradle.kts (Project: androidProject)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android.gradle.plugin) apply false

    // (★ 수정 ★) 'Safe Args' '플러그인' '설정'이 '여기에' '있어야' '합니다'.
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
}