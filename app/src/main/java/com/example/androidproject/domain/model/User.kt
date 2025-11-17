package com.example.androidproject.domain.model

data class User(
    val id: String,
    val password: String,
    val name: String,
    val gender: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val activityLevel: String,
    val fitnessGoal: String,
    val allergyInfo: List<String>,

    // (GetAIRecommendationUseCase에서 참조됨)
    val preferredDietType: String, // 기존 필드 (preferredDietaryTypes 대신 사용)
    val preferredDietaryTypes: List<String>, // 이 필드는 List<String> 타입으로 새로 추가해야 합니다.
    val equipmentAvailable: List<String>,
    val currentPainLevel: Int, // 1-10 스케일
    val additionalNotes: String? = null,

    val targetCalories: Int? = null,
    val currentInjuryId: String? = null
)