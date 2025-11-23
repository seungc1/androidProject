// build.gradle.kts (Project: androidProject)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android.gradle.plugin) apply false
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false
}