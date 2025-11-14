package com.example.androidproject.data.mapper

import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.remote.dto.AIRecommendationResultDto
import com.example.androidproject.data.remote.dto.DietDto
import com.example.androidproject.data.remote.dto.ExerciseDto
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise

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
        recommendedExercises = emptyList(), // 임시로 비어있는 운동 목록
        recommendedDiets = emptyList(),     // 임시로 비어있는 식단 목록
        overallSummary = "AI 추천을 불러오는 중입니다..." // 임시 요약
    )
    // ⬆️ --- [임시 코드] --- ⬆️
}