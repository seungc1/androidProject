// build.gradle.kts (Project:YourAppName)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android.gradle.plugin) apply false
}