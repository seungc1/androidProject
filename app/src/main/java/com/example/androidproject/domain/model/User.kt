package com.example.androidproject.domain.model

/**
 * 사용자의 개인 정보 및 건강 관련 설정, AI 추천의 기반이 되는 데이터.
 * Domain Layer Model
 */
data class User(
    /** 사용자를 식별하는 고유 ID. */
    val id: String,

    /** 사용자의 이름. */
    val name: String,

    /** 성별 (예: "남성", "여성"). 식단 및 운동 강도 추천에 활용. */
    val gender: String,

    /** 나이. 운동 강도 및 영양 요구량에 영향. */
    val age: Int,

    /** 키 (센티미터). */
    val heightCm: Int,

    /** 몸무게 (Kg). BMI 계산 및 식단/운동 목표 설정에 활용. */
    val weightKg: Double,

    /** 활동 수준 (예: "비활동적", "활동적", "매우 활동적"). */
    val activityLevel: String,

    /** 피트니스 목표 (예: "체중 감량", "근육 증가", "유지"). */
    val fitnessGoal: String,

    /** 알레르기 유발 식품 목록 (예: "땅콩", "유제품"). */
    val allergyInfo: List<String>,

    /** 선호하는 식단 유형 (예: "비건", "저탄수화물", "일반"). */
    val preferredDietType: String,

    /** 목표 일일 칼로리 섭취량 (선택 사항). 사용자 설정 또는 AI 추천. */
    val targetCalories: Int?,

    /** 현재 겪고 있는 부상의 ID (선택 사항). 운동 추천 시 제약 조건으로 활용. */
    val currentInjuryId: String?
)
