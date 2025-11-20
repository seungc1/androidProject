// 파일 경로: app/src/main/java/com/example/androidproject/domain/repository/DietRepository.kt
package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    // upsertDiets만 suspend 유지 (DB 쓰기)
    suspend fun upsertDiets(diets: List<Diet>)

    // ★★★ [수정] Flow 반환 시 suspend 키워드 제거 ★★★
    fun getDietById(dietId: String): Flow<Diet?>
}