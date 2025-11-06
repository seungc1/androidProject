// app/src/main/java/com/example/androidproject/domain/repository/RehabRepository.kt
package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface RehabRepository {
    // 특정 운동의 상세 정보 가져오기
    suspend fun getExerciseDetail(exerciseId: String): Flow<Exercise>

    // (옵션) 모든 운동 목록 가져오기 - AI 추천 외에 브라우징 기능을 위한 것.
    // 필요하다면 나중에 추가하거나, GetAIRecommendationUseCase에서 다 가져올 수도 있음.
    // suspend fun getAllExercises(): Flow<List<Exercise>>
}