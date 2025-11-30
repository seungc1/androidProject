// app/src/main/java/com/example/androidproject/domain/model/RecommendationParams.kt
package com.dataDoctor.rehabai.domain.model

/**
 * AI (GPT) 모델에 재활 운동 및 식단 추천을 요청하기 위한 파라미터.
 * 사용자의 현재 상태와 목표에 대한 상세 정보를 포함합니다.
 * 이 DTO는 User 및 Injury 모델의 핵심 정보를 AI API로 전달하기 위해 사용됩니다.
 */
data class RecommendationParams(
    val userId: String,
    val age: Int,
    val gender: String,
    val heightCm: Int,
    val weightKg: Double,
    val activityLevel: String, // 예: "Sedentary", "Lightly active"
    val fitnessGoal: String, // 예: "Strength Recovery", "Weight Loss"
    val dietaryPreferences: List<String>, // 예: ["Vegetarian", "No Dairy", "Low Carb"]
    val allergies: List<String>, // 예: ["Peanuts", "Shellfish"]
    val equipmentAvailable: List<String>, // 예: ["Dumbbells", "Resistance Bands"]
    val currentPainLevel: Int, // 1-10 스케일
    val injuryArea: String? = null, // 예: "Left Knee", "Shoulder"
    val injuryType: String? = null, // 예: "Ligament Sprain", "Post-surgery recovery"
    val injurySeverity: String? = null, // 예: "심각", "보통", "경미"
    val additionalNotes: String? = null, // GPT에게 전달할 추가적인 자유 형식 정보
    val pastSessions: List<RehabSession>
)