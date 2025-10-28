// app/src/main/java/com/example/androidproject/data/repository/RehabRepositoryImpl.kt
package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.repository.RehabRepository
import javax.inject.Inject

// RehabRepository 인터페이스의 실제 구현체 (데이터 출처 관리)
class RehabRepositoryImpl @Inject constructor(
    // 나중에 LocalDataSource, RemoteDataSource 등을 주입받을 예정
) : RehabRepository {

    // 초기에는 더미 데이터를 반환하여 동작을 확인합니다.
    // 실제 앱에서는 RemoteDataSource (서버 API) 또는 LocalDataSource (Room DB)에서 데이터를 가져옵니다.
    override suspend fun getRecommendedExercises(injury: Injury): List<Exercise> {
        // 예를 들어, 모든 운동 목록을 반환한다고 가정
        return generateDummyExercises()
    }

    override suspend fun getAllExercisesByBodyPart(bodyPart: String): List<Exercise> {
        // 특정 부위 관련 더미 운동 목록 반환
        return generateDummyExercises().filter { it.targetBodyPart == bodyPart || it.riskBodyParts.contains(bodyPart) }
    }

    // 더미 데이터 생성 함수
    private fun generateDummyExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = "EX001",
                name = "손목 스트레칭",
                description = "손목의 유연성을 높이는 스트레칭입니다.",
                targetBodyPart = "손목",
                riskBodyParts = listOf(), // 이 운동은 거의 부담 없음
                videoUrl = "https://example.com/wrist_stretch.mp4",
                difficulty = 1
            ),
            Exercise(
                id = "EX002",
                name = "어깨 회전 운동",
                description = "어깨 관절의 가동 범위를 늘립니다.",
                targetBodyPart = "어깨",
                riskBodyParts = listOf(),
                videoUrl = "https://example.com/shoulder_rotation.mp4",
                difficulty = 2
            ),
            Exercise(
                id = "EX003",
                name = "팔굽혀펴기",
                description = "가슴, 어깨, 삼두근을 강화하는 운동입니다.",
                targetBodyPart = "가슴",
                riskBodyParts = listOf("손목", "어깨", "팔꿈치"), // 손목, 어깨에 부담을 줄 수 있음
                videoUrl = "https://example.com/pushup.mp4",
                difficulty = 3
            ),
            Exercise(
                id = "EX004",
                name = "플랭크",
                description = "코어 근육을 강화하는 등척성 운동입니다.",
                targetBodyPart = "코어",
                riskBodyParts = listOf("손목", "어깨", "허리"), // 손목, 어깨, 허리에 부담을 줄 수 있음
                videoUrl = "https://example.com/plank.mp4",
                difficulty = 3
            ),
            Exercise(
                id = "EX005",
                name = "무릎 굽히기 스트레칭",
                description = "무릎 주변 근육의 긴장을 완화합니다.",
                targetBodyPart = "무릎",
                riskBodyParts = listOf("무릎"), // 무릎 부상 시 주의
                videoUrl = "https://example.com/knee_stretch.mp4",
                difficulty = 1
            )
        )
    }
}