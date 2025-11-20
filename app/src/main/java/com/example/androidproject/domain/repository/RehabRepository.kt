// 파일 경로: app/src/main/java/com/example/androidproject/domain/repository/RehabRepository.kt
package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface RehabRepository {
    // ★★★ [수정] Flow 반환 시 suspend 키워드 제거 (Kapt 오류 해결) ★★★
    fun getExerciseDetail(exerciseId: String): Flow<Exercise?>
}