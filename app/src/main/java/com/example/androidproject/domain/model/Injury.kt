// app/src/main/java/com/example/androidproject/domain/model/Injury.kt
package com.example.androidproject.domain.model

// 사용자의 부상 정보를 나타내는 데이터 클래스
data class Injury(
    val bodyPart: String, // 손상 부위 (예: "손목", "무릎", "허리")
    val painLevel: Int,   // 통증 수준 (예: 0-10 척도)
    val diagnosis: String? = null // 진단명 (선택 사항)
)