// app/src/main/java/com/example/androidproject/domain/model/Injury.kt
package com.dataDoctor.rehabai.domain.model

// 사용자의 부상 정보를 나타내는 데이터 클래스
data class Injury(
    val id: String,
    val name: String, // 예: "손목 염좌", "어깨 회전근개 부상"
    val bodyPart: String, // 예: "손목", "어깨"
    val severity: String, // 예: "경미", "보통", "심각"
    val description: String // 부상에 대한 상세 설명
)