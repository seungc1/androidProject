package com.example.androidproject.domain.model


import java.util.Date

data class RehabSession(
    val id: String,
    val userId: String,
    val exerciseId: String,
    val dateTime: Date, // 운동 기록 시간
    val sets: Int, // 세트 수
    val reps: Int, // 반복 횟수
    val durationMinutes: Int?, // 지속 시간 (분)
    val userRating: Int?, // 1~5점 등 사용자 만족도
    val notes: String? // 주관적인 피드백 (이전의 userFeedback)
)