package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import javax.inject.Inject

// AIApiRepository 인터페이스의 실제 구현체 (AI API 통신 담당)
class AIApiRepositoryImpl @Inject constructor(
    // 나중에 RetrofitService 등을 주입받을 예정
) : AIApiRepository {

    override suspend fun getAIRehabAndDietRecommendation(
        userInfo: User,
        injuryInfo: Injury?
    ): Flow<AIRecommendationResult> {
        // 실제 구현에서는 AI API를 호출하고 응답을 파싱하여 반환합니다.
        // 현재는 더미 데이터를 반환합니다.

        // 더미 운동 데이터 (userInfo와 injuryInfo에 따라 간단하게 조정 가능)
        val dummyExercises = listOf(
            Exercise(
                id = "AI_EX001",
                name = "${injuryInfo?.bodyPart ?: "전신"} 부위 추천 스트레칭",
                description = "AI가 ${userInfo.name}님에게 추천하는 부드러운 스트레칭입니다.",
                bodyPart = injuryInfo?.bodyPart ?: "전신",
                difficulty = "초급",
                videoUrl = "https://example.com/ai_stretch.mp4",
                precautions = injuryInfo?.let { "${it.bodyPart} 부상 시 주의" } ?: null,
                aiRecommendationReason = "${userInfo.fitnessGoal} 목표와 ${injuryInfo?.bodyPart ?: "현재 상태"}를 고려한 추천."
            ),
            Exercise(
                id = "AI_EX002",
                name = "${userInfo.fitnessGoal}을 위한 코어 운동",
                description = "AI가 ${userInfo.name}님의 ${userInfo.fitnessGoal}에 맞춰 추천하는 코어 강화 운동입니다.",
                bodyPart = "코어",
                difficulty = "중급",
                videoUrl = "https://example.com/ai_core.mp4",
                precautions = null,
                aiRecommendationReason = "${userInfo.fitnessGoal} 목표 달성을 위한 핵심 운동입니다."
            )
        )

        // 더미 식단 데이터 (userInfo에 따라 간단하게 조정 가능)
        val dummyDiets = listOf(
            Diet(
                id = "AI_D001",
                mealType = "아침",
                foodName = "고단백 오트밀",
                quantity = 150.0,
                unit = "g",
                calorie = 350,
                protein = 25.0,
                fat = 10.0,
                carbs = 40.0,
                ingredients = listOf("오트밀", "프로틴 파우더", "베리류", "견과류"),
                preparationTips = "물 또는 우유에 타서 전자레인지 2분.",
                aiRecommendationReason = "${userInfo.fitnessGoal} 목표에 맞는 고단백 아침 식단입니다."
            ),
            Diet(
                id = "AI_D002",
                mealType = "점심",
                foodName = "닭가슴살 샐러드",
                quantity = 300.0,
                unit = "g",
                calorie = 400,
                protein = 40.0,
                fat = 15.0,
                carbs = 20.0,
                ingredients = listOf("닭가슴살", "혼합 채소", "올리브 오일", "발사믹 식초"),
                preparationTips = "신선한 채소와 함께 드세요.",
                aiRecommendationReason = "${userInfo.fitnessGoal} 목표에 맞춰 단백질을 충분히 섭취할 수 있는 점심 식단입니다."
            )
        )

        val dummyResult = AIRecommendationResult(
            recommendedExercises = dummyExercises,
            recommendedDiets = dummyDiets
        )

        return flowOf(dummyResult)
    }
}