package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.Exercise
// import com.example.androidproject.domain.model.Injury // 이제 필요 없음
import com.example.androidproject.domain.repository.RehabRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf // Flow를 반환하기 위해 필요

// RehabRepository 인터페이스의 실제 구현체 (데이터 출처 관리)
class RehabRepositoryImpl @Inject constructor(
    // 나중에 LocalDataSource, RemoteDataSource 등을 주입받을 예정
) : RehabRepository {

    // ⭐ 1. getExerciseDetail 함수 구현 추가 ⭐
    override suspend fun getExerciseDetail(exerciseId: String): Flow<Exercise> {
        // 실제 구현에서는 LocalDataSource나 RemoteDataSource에서 데이터를 가져옵니다.
        // 여기서는 더미 데이터에서 찾아서 반환합니다.
        val exercise = generateDummyExercises().firstOrNull { it.id == exerciseId }
            ?: throw NoSuchElementException("Exercise with id $exerciseId not found")
        return flowOf(exercise)
    }

    // 더미 데이터 생성 함수 (필드 이름 수정)
    private fun generateDummyExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = "EX001",
                name = "손목 스트레칭",
                description = "손목의 유연성을 높이는 스트레칭입니다.",
                bodyPart = "손목", // ⭐ targetBodyPart -> bodyPart로 수정 ⭐
                difficulty = "초급", // ⭐ difficulty는 String으로 변경되었으므로 수정 ⭐
                videoUrl = "https://example.com/wrist_stretch.mp4",
                precautions = null // ⭐ riskBodyParts -> precautions로 변경되었으므로 수정 ⭐
            ),
            Exercise(
                id = "EX002",
                name = "어깨 회전 운동",
                description = "어깨 관절의 가동 범위를 늘립니다.",
                bodyPart = "어깨", // ⭐ targetBodyPart -> bodyPart로 수정 ⭐
                difficulty = "중급", // ⭐ difficulty는 String으로 변경되었으므로 수정 ⭐
                videoUrl = "https://example.com/shoulder_rotation.mp4",
                precautions = null
            ),
            Exercise(
                id = "EX003",
                name = "팔굽혀펴기",
                description = "가슴, 어깨, 삼두근을 강화하는 운동입니다.",
                bodyPart = "가슴", // ⭐ targetBodyPart -> bodyPart로 수정 ⭐
                difficulty = "중급", // ⭐ difficulty는 String으로 변경되었으므로 수정 ⭐
                videoUrl = "https://example.com/pushup.mp4",
                precautions = "손목, 어깨, 팔꿈치에 부담을 줄 수 있습니다." // ⭐ riskBodyParts -> precautions로 변경되었으므로 수정 ⭐
            ),
            Exercise(
                id = "EX004",
                name = "플랭크",
                description = "코어 근육을 강화하는 등척성 운동입니다.",
                bodyPart = "코어", // ⭐ targetBodyPart -> bodyPart로 수정 ⭐
                difficulty = "중급", // ⭐ difficulty는 String으로 변경되었으므로 수정 ⭐
                videoUrl = "https://example.com/plank.mp4",
                precautions = "손목, 어깨, 허리에 부담을 줄 수 있습니다." // ⭐ riskBodyParts -> precautions로 변경되었으므로 수정 ⭐
            ),
            Exercise(
                id = "EX005",
                name = "무릎 굽히기 스트레칭",
                description = "무릎 주변 근육의 긴장을 완화합니다.",
                bodyPart = "무릎", // ⭐ targetBodyPart -> bodyPart로 수정 ⭐
                difficulty = "초급", // ⭐ difficulty는 String으로 변경되었으므로 수정 ⭐
                videoUrl = "https://example.com/knee_stretch.mp4",
                precautions = "무릎 부상 시 주의하십시오." // ⭐ riskBodyParts -> precautions로 변경되었으므로 수정 ⭐
            )
        )
    }
}