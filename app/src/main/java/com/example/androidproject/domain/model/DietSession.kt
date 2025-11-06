package com.example.androidproject.domain.model

import java.time.LocalDateTime

data class DietSession(
    val id: String,
    val userId: String,
    val dietId: String, // 기록된 Diet의 ID (Diet 모델이 외부 API로부터 온 것일 경우)
    val dateTime: LocalDateTime, // 식단 기록 시간
    val actualQuantity: Double, // 실제 섭취량 (Diet 모델의 quantity와 다를 수 있음)
    val actualUnit: String, // 실제 섭취 단위
    val userSatisfaction: Int? // 사용자 만족도 (1~5점 등, 선택 사항)
)