// app/src/test/java/com/example/androidproject/data/repository/FakeRehabRepository.kt
package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.repository.RehabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRehabRepository : RehabRepository {
    // 테스트용 더미 운동 목록
    val fakeExercises = mutableListOf(
        Exercise("ex1", "기본 스트레칭", "전신을 이완합니다.", "전신", "초급", "url_stretch", null, null),
        Exercise("ex2", "가벼운 걷기", "유산소 운동입니다.", "하체", "초급", "url_walk", null, null)
    )

    override suspend fun getExerciseDetail(exerciseId: String): Flow<Exercise> {
        return flowOf(fakeExercises.first { it.id == exerciseId })
    }
}