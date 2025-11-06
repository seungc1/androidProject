package com.example.androidproject.domain.model

data class User(
    val id: String,
    val name: String,
    val gender: String, // 예: "남성", "여성"
    val age: Int,
    val heightCm: Int, // 키 (cm)
    val weightKg: Double, // 몸무게 (kg)
    val activityLevel: String, // 예: "비활동적", "활동적", "매우 활동적"
    val fitnessGoal: String, // 예: "체중 감량", "근육 증가", "유지"
    val allergyInfo: List<String>, // 알레르기 정보 (예: "땅콩", "유제품")
    val preferredDietType: String, // 선호 식단 유형 (예: "비건", "저탄수화물", "일반")
    val targetCalories: Int? = null, // 목표 칼로리 (선택 사항)
    val currentInjuryId: String? = null // 현재 부상 ID (선택 사항)
)