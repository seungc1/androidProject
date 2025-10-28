// app/src/main/java/com/example/androidproject/domain/model/Exercise.kt
package com.example.androidproject.domain.model

// 재활 운동 정보를 나타내는 데이터 클래스
data class Exercise(
    val id: String,         // 운동 고유 ID
    val name: String,       // 운동 이름 (예: "손목 스트레칭")
    val description: String,// 운동 설명
    val targetBodyPart: String, // 주 타겟 신체 부위
    val riskBodyParts: List<String>, // 이 운동이 부담을 줄 수 있는 신체 부위 목록 (예: ["손목", "어깨"])
    val videoUrl: String? = null, // 운동 가이드 영상 URL (초기에는 null 또는 더미 값)
    val difficulty: Int = 1 // 운동 난이도 (1:쉬움, 5:어려움)
)