// app/src/main/java/com.example/androidproject/domain/model/RehabData.kt

package com.example.androidproject.domain.model

/**
 * AI (GPT) 모델에 사용자의 재활 진행 상황 분석을 요청하기 위한 기록 데이터.
 * 과거 재활 세션 및 식단 세션 정보를 포함합니다.
 */
data class RehabData(
    val userId: String,
    val userProfile: User, // 현재 사용자 프로필
    val pastRehabSessions: List<RehabSession>, // 과거 운동 기록
    val pastDietSessions: List<DietSession>, // 과거 식단 기록
    val currentPainLevel: Int, // 현재 통증 수준 (분석 시점)
    val userFeedback: String? = null // 사용자가 AI에게 직접 전달하고 싶은 피드백
)