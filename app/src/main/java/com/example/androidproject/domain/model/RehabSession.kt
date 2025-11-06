package com.example.androidproject.domain.model

import java.time.LocalDateTime

data class RehabSession(
    val id: String,
    val userId: String,
    val exerciseId: String,
    val dateTime: LocalDateTime, // 운동 기록 시간
    val sets: Int, // 세트 수
    val reps: Int, // 반복 횟수
    val durationMinutes: Int?, // 지속 시간 (분)
    val successStatus: String, // 예: "성공", "부분 성공", "실패"
    val userFeedback: String? // 사용자의 자체 피드백 (선택 사항)
)