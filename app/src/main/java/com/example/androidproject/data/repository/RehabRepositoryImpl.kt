package com.example.androidproject.data.repository

// ✅ [추가] LocalDataSource와 Mapper를 import
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.repository.RehabRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map // ✅ [추가] Flow.map을 import
import java.util.NoSuchElementException // ✅ [추가]

// ✅ [수정] Hilt가 LocalDataSource를 주입하도록 생성자 변경
class RehabRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource // ✅ [추가]
) : RehabRepository {

    override suspend fun getExerciseDetail(exerciseId: String): Flow<Exercise> {
        // ✅ [수정] LocalDataSource에서 데이터를 조회하고 'toDomain'으로 번역
        return localDataSource.getExerciseById(exerciseId).map { exerciseEntity ->
            exerciseEntity?.toDomain()
                ?: throw NoSuchElementException("Exercise with id $exerciseId not found")
        }
    }
}