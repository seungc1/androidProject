// app/src/main/java/com/example/androidproject/domain/repository/RehabRepository.kt
package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury

// 재활 관련 데이터를 가져오는 계약(Interface)
interface RehabRepository {
    // 사용자의 부상 정보를 기반으로 추천 운동 목록을 가져오는 함수
    suspend fun getRecommendedExercises(injury: Injury): List<Exercise>

    // 특정 신체 부위에 대한 모든 재활 운동 목록을 가져오는 함수 (옵션)
    suspend fun getAllExercisesByBodyPart(bodyPart: String): List<Exercise>
}