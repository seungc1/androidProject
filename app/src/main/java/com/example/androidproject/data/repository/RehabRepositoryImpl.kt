// 파일 경로: app/src/main/java/com/example/androidproject/data/repository/RehabRepositoryImpl.kt
package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.repository.RehabRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RehabRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : RehabRepository {

    // ★★★ [수정] Flow 반환 시 suspend 키워드 제거 ★★★
    override fun getExerciseDetail(exerciseId: String): Flow<Exercise?> {
        return localDataSource.getExerciseById(exerciseId).map { exerciseEntity ->
            exerciseEntity?.toDomain()
        }
    }
}