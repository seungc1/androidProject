package com.dataDoctor.rehabai.data.mapper

import com.dataDoctor.rehabai.data.local.entity.DietEntity
import com.dataDoctor.rehabai.data.local.entity.DietSessionEntity
import com.dataDoctor.rehabai.data.local.entity.ExerciseEntity
import com.dataDoctor.rehabai.data.local.entity.InjuryEntity
import com.dataDoctor.rehabai.data.local.entity.RehabSessionEntity
import com.dataDoctor.rehabai.data.local.entity.UserEntity
import com.dataDoctor.rehabai.data.remote.dto.AIRecommendationResultDto
import com.dataDoctor.rehabai.data.remote.dto.DietDto
import com.dataDoctor.rehabai.data.remote.dto.ExerciseDto
import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.Diet
import com.dataDoctor.rehabai.domain.model.DietRecommendation
import com.dataDoctor.rehabai.domain.model.DietSession
import com.dataDoctor.rehabai.domain.model.Exercise
import com.dataDoctor.rehabai.domain.model.Injury
import com.dataDoctor.rehabai.domain.model.RehabSession
import com.dataDoctor.rehabai.domain.model.User
import java.util.UUID

/**
 * 데이터 계층(DTO, Entity)의 모델을 도메인 계층(Domain Model)의 모델로
 * '번역'해주는 확장 함수(Mapper)들입니다.
 */

fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason,
        sets = null, // (DTO에 sets/reps가 없으므로 null로 설정)
        reps = null,
        imageName = null // DTO는 imageName을 모르므로 null
    )
}

// (Entity -> Domain)
fun ExerciseEntity.toDomain(): Exercise {
    return Exercise(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason,
        sets = null, // (Entity에 sets/reps가 없으므로 null로 설정)
        reps = null,
        imageName = this.imageName // ★★★ Entity에서 Domain으로 imageName 전달 ★★★
    )
}

// (Domain -> Entity)
fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        bodyPart = this.bodyPart,
        difficulty = this.difficulty,
        precautions = this.precautions,
        aiRecommendationReason = this.aiRecommendationReason,
        imageName = this.imageName // ★★★ Domain에서 Entity로 imageName 전달 ★★★
    )
}
// --- Diet Mappers ---

// (DTO -> Domain)
fun DietDto.toDomain(): Diet {
    return Diet(
        id = this.id,
        mealType = this.mealType,
        foodName = this.foodName,
        quantity = this.quantity,
        unit = this.unit,
        calorie = this.calorie,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        ingredients = this.ingredients,
        preparationTips = this.preparationTips,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

fun Diet.toEntity(): DietEntity {
    return DietEntity(
        id = this.id,
        mealType = this.mealType,
        foodName = this.foodName,
        quantity = this.quantity,
        unit = this.unit,
        calorie = this.calorie,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        ingredients = this.ingredients.joinToString(","), // List -> String
        preparationTips = this.preparationTips,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

fun DietEntity.toDomain(): Diet {
    return Diet(
        id = this.id,
        mealType = this.mealType,
        foodName = this.foodName,
        quantity = this.quantity,
        unit = this.unit,
        calorie = this.calorie,
        protein = this.protein,
        fat = this.fat,
        carbs = this.carbs,
        ingredients = this.ingredients.split(",").map { it.trim() }, // String -> List
        preparationTips = this.preparationTips,
        aiRecommendationReason = this.aiRecommendationReason
    )
}

fun DietRecommendation.toDomain(): Diet {
    return Diet(
        id = this.foodItems?.joinToString() ?: UUID.randomUUID().toString(),
        mealType = this.mealType,
        foodName = this.foodItems?.joinToString(", ") ?: "이름 없는 식단",
        quantity = 1.0, // (AI가 '양'을 주지 않았으므로 기본값 1.0)
        unit = "인분",  // (AI가 '단위'를 주지 않았으므로 기본값 '인분')
        calorie = this.calories?.toInt() ?: 0,
        protein = this.proteinGrams ?: 0.0,
        fat = this.fats ?: 0.0,
        carbs = this.carbs ?: 0.0,
        ingredients = this.ingredients ?: emptyList(),
        preparationTips = null, // (AI가 준 tips가 있다면 여기에 매핑)
        aiRecommendationReason = this.aiRecommendationReason
    )
}

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
        allergyInfo = if (this.allergyInfo.isBlank()) emptyList() else this.allergyInfo.split(",").map { it.trim() },
        preferredDietType = this.preferredDietType,
        targetCalories = this.targetCalories,
        currentInjuryId = this.currentInjuryId,
        preferredDietaryTypes = if (this.preferredDietaryTypes.isBlank()) emptyList() else this.preferredDietaryTypes.split(",").map { it.trim() },
        equipmentAvailable = if (this.equipmentAvailable.isBlank()) emptyList() else this.equipmentAvailable.split(",").map { it.trim() },
        currentPainLevel = this.currentPainLevel,
        additionalNotes = this.additionalNotes
    )
}

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
        allergyInfo = this.allergyInfo.joinToString(", "),
        preferredDietType = this.preferredDietType,
        targetCalories = this.targetCalories,
        currentInjuryId = this.currentInjuryId,
        preferredDietaryTypes = this.preferredDietaryTypes.joinToString(", "),
        equipmentAvailable = this.equipmentAvailable.joinToString(", "),
        currentPainLevel = this.currentPainLevel,
        additionalNotes = this.additionalNotes
    )
}


fun Injury.toEntity(userId: String): InjuryEntity {
    return InjuryEntity(
        id = this.id,
        userId = userId,
        name = this.name,
        bodyPart = this.bodyPart,
        severity = this.severity,
        description = this.description
    )
}

fun InjuryEntity.toDomain(): Injury {
    return Injury(
        id = this.id,
        name = this.name,
        bodyPart = this.bodyPart,
        severity = this.severity,
        description = this.description
    )
}


// --- AI Result Mapper ---
fun AIRecommendationResultDto.toDomain(): AIRecommendationResult {
    return AIRecommendationResult(
        scheduledWorkouts = emptyList(),

        //  누락된 파라미터 추가
        scheduledDiets = emptyList(),

        overallSummary = "AI 추천을 불러오는 중입니다...",
        disclaimer = ""
    )
}

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

fun DietSession.toEntity(): DietSessionEntity {
    return DietSessionEntity(
        id = this.id,
        userId = this.userId,
        dietId = this.dietId,
        dateTime = this.dateTime,
        actualQuantity = this.actualQuantity,
        actualUnit = this.actualUnit,
        userSatisfaction = this.userSatisfaction,
        notes = this.notes,
        foodName = this.foodName, // [추가]
        photoUrl = this.photoUrl // [추가]
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
        notes = this.notes,
        foodName = this.foodName, // [추가]
        photoUrl = this.photoUrl // [추가]
    )
}