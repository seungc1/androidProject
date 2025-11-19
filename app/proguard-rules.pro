# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Gson 라이브러리 규칙 (필수) ---
# Gson이 제네릭 타입(List<T> 등)을 인식하기 위해 필요합니다.
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# TypeToken을 사용하는 코드가 난독화되지 않도록 보호합니다.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- 데이터 모델 클래스 보호 ---
# 앱의 데이터 클래스(User, Exercise 등)의 필드명이 바뀌면 JSON 파싱이 실패하므로 보호합니다.
-keep class com.example.androidproject.domain.model.** { *; }
-keep class com.example.androidproject.data.local.entity.** { *; }
-keep class com.example.androidproject.data.remote.dto.** { *; }

# --- Retrofit 관련 규칙 ---
-keep class retrofit2.** { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**