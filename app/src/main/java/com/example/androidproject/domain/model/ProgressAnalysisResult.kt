// app/src/main/java/com/example/androidproject/domain/model/ProgressAnalysisResult.kt
package com.example.androidproject.domain.model

// 사용자의 재활 및 식단 진행 분석 결과를 담을 모델
data class ProgressAnalysisResult(
    val totalRehabSessions: Int,
    val totalDietSessions: Int,
    val averageRehabRating: Double,
    val averageDietSatisfaction: Double,
    val feedbackMessage: String // 분석 결과에 따른 사용자 피드백 메시지
)