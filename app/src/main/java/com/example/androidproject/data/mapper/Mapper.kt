package com.example.androidproject.data.mapper

import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.remote.dto.AIRecommendationResultDto
import com.example.androidproject.data.remote.dto.DietDto
import com.example.androidproject.data.remote.dto.ExerciseDto
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.User
import com.example.androidproject.data.local.entity.UserEntity
import com.example.androidproject.data.local.entity.RehabSessionEntity
import com.example.androidproject.data.local.entity.DietSessionEntity
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.model.DietSession

/**
 * 데이터 계층(DTO, Entity)의 모델을 도메인 계층(Domain Model)의 모델로
 * '번역'해주는 확장 함수(Mapper)들입니다.
 *
 * [최종 수정] 2:33 AM (Exercise) / 2:35 AM (Diet) 모델 기준
 */

// --- Exercise Mappers ---
// (2:33 AM에 주신 Exercise 모델 기준 - DTO/Entity와 필드가 동일함)

fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        videoUrl = this.videoUrl,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

fun ExerciseEntity.toDomain(): Exercise {
    return Exercise(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        videoUrl = this.videoUrl,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        videoUrl = this.videoUrl,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

// --- Diet Mappers ---
// (2:35 AM에 주신 Diet 모델 기준)

fun DietDto.toDomain(): Diet {
    return Diet(
        id = this.id,
        mealType = this.mealType,
        foodName = this.foodName,
        quantity = this.quantity,
        unit = this.unit,
        // ✅ [수정] Dto(Int) -> Domain(Int). 타입 변환 불필요
        calorie = this.calorie,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        ingredients = this.ingredients,
        // ✅ [수정] Dto(String?) -> Domain(String?). null 처리(?: "") 불필요
        preparationTips = this.preparationTips,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

// --- User Mappers ---
// (파일 맨 아래에 추가)

/**
 * UserEntity (DB)를 User (Domain)로 변환합니다.
 */
fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        password = this.password,
        name = this.name,
        gender = this.gender,
        age = this.age,
        heightCm = this.heightCm,
        weightKg = this.weightKg,
        activityLevel = this.activityLevel,
        fitnessGoal = this.fitnessGoal,
        // ✅ [수정] DB(String) -> Domain(List<String>)
        allergyInfo = this.allergyInfo.split(",").map { it.trim() },
        preferredDietType = this.preferredDietType,
        targetCalories = this.targetCalories,
        currentInjuryId = this.currentInjuryId,
        // ✅ [수정] Domain 모델에 맞춰 '가짜' 기본값 추가 (DB Entity에 이 필드들이 없음)
        preferredDietaryTypes = emptyList(),
        equipmentAvailable = emptyList(),
        currentPainLevel = 0,
        additionalNotes = null
    )
}

/**
 * User (Domain)를 UserEntity (DB)로 변환합니다.
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        password = this.password,
        name = this.name,
        gender = this.gender,
        age = this.age,
        heightCm = this.heightCm,
        weightKg = this.weightKg,
        activityLevel = this.activityLevel,
        fitnessGoal = this.fitnessGoal,
        // ✅ [수정] Domain(List<String>) -> DB(String)
        allergyInfo = this.allergyInfo.joinToString(", "),
        preferredDietType = this.preferredDietType,
        targetCalories = this.targetCalories,
        currentInjuryId = this.currentInjuryId
    )
}


// --- AI Result Mapper ---

fun AIRecommendationResultDto.toDomain(): AIRecommendationResult {

    // ⬇️ --- [임시 주석 처리] --- ⬇️
    // "설계도 충돌" 오류가 나는 기존 코드입니다.
    // return AIRecommendationResult(
    //     recommendedExercises = this.recommendedExercises.map { it.toDomain() },
    //     recommendedDiets = this.recommendedDiets.map { it.toDomain() }
    // )
    // ⬆️ --- [임시 주석 처리] --- ⬆️


    // ⬇️ --- [임시 코드] --- ⬇️
    // 빌드 오류를 해결하기 위해, '진짜' 모델에 맞춰 비어있는 객체를 반환합니다.
    // (팀과 논의 후, 이 코드는 삭제하고 위의 주석을 풀어야 합니다.)
    return AIRecommendationResult(
        // ✅ [수정] 'recommendedExercises' -> 'scheduledWorkouts'로 변경
        scheduledWorkouts = emptyList(),
        recommendedDiets = emptyList(),
        overallSummary = "AI 추천을 불러오는 중입니다..."
    )
    // ⬆️ --- [임시 코드] --- ⬆️

    // ... (AI Result Mapper 뒤, 파일 맨 끝) ...
}
// --- RehabSession Mappers ---

    fun RehabSession.toEntity(): RehabSessionEntity {
        return RehabSessionEntity(
            id = this.id,
            userId = this.userId,
            exerciseId = this.exerciseId,
            dateTime = this.dateTime,
            sets = this.sets,
            reps = this.reps,
            durationMinutes = this.durationMinutes,
            userRating = this.userRating,
            notes = this.notes
        )
    }

    fun RehabSessionEntity.toDomain(): RehabSession {
        return RehabSession(
            id = this.id,
            userId = this.userId,
            exerciseId = this.exerciseId,
            dateTime = this.dateTime,
            sets = this.sets,
            reps = this.reps,
            durationMinutes = this.durationMinutes,
            userRating = this.userRating,
            notes = this.notes
        )
    }

// --- DietSession Mappers ---

    fun DietSession.toEntity(): DietSessionEntity {
        return DietSessionEntity(
            id = this.id,
            userId = this.userId,
            dietId = this.dietId,
            dateTime = this.dateTime,
            actualQuantity = this.actualQuantity,
            actualUnit = this.actualUnit,
            userSatisfaction = this.userSatisfaction,
            notes = this.notes
        )
    }

    fun DietSessionEntity.toDomain(): DietSession {
        return DietSession(
            id = this.id,
            userId = this.userId,
            dietId = this.dietId,
            dateTime = this.dateTime,
            actualQuantity = this.actualQuantity,
            actualUnit = this.actualUnit,
            userSatisfaction = this.userSatisfaction,
            notes = this.notes
        )
    }


