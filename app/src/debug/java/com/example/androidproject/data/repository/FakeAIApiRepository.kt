package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.DietRecommendation
import com.example.androidproject.domain.model.ExerciseRecommendation
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.repository.AIApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * 'debug' 빌드 시 주입될 가짜(Fake) Repository입니다.
 * 실제 API를 호출하지 않고, 더미 데이터를 반환합니다.
 */
class FakeAIApiRepository @Inject constructor() : AIApiRepository {

    /**
     * (기존 기능) AI 추천 더미 데이터
     */
    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {

        val dummyExercises = listOf(
            ExerciseRecommendation(
                name = "팔굽혀펴기 변형 (Debug)",
                description = "${params.gender}님, ${params.injuryArea} 부상을 고려한 팔굽혀펴기입니다.",
                bodyPart = "가슴, 어깨", sets = 3, reps = 10, difficulty = "중급",
                aiRecommendationReason = "부상 부위 외 주변 근육 강화에 도움을 줍니다.",
                imageUrl = null
            )
        )
        val dummyDiets = listOf(
            DietRecommendation(
                mealType = "점심",
                foodItems = listOf("닭가슴살 샐러드 (Debug)", "고구마 100g"),
                ingredients = listOf("닭가슴살", "양상추", "토마토", "고구마"),
                calories = 400.0, proteinGrams = 30.0, carbs = 40.0, fats = 15.0,
                aiRecommendationReason = "${params.fitnessGoal}에 필요한 고단백, 저탄수 식단입니다."
            )
        )
        val aiResult = AIRecommendationResult(
            recommendedExercises = dummyExercises,
            recommendedDiets = dummyDiets,
            overallSummary = "${params.gender}님의 ${params.injuryArea} 부상과 ${params.fitnessGoal}을 위한 맞춤형 추천입니다. (Debug 모드)",
            disclaimer = "더미 데이터로 생성된 추천입니다."
        )

        emit(aiResult)
    }

    /**
     *  AI 주간 분석 리포트 더미 데이터
     * (AIApiRepository 인터페이스에 추가된 함수를 구현합니다)
     */
    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> {
        val fakeAnalysis = AIAnalysisResult(
            summary = "AI 주간 분석 (Debug 모드): 지난 주 운동은 꾸준했지만, 식단 기록이 부족합니다.",
            strengths = listOf("운동을 꾸준히 했습니다."),
            areasForImprovement = listOf("식단 기록이 부족합니다.", "운동 후 '힘들다'는 피드백이 많습니다."),
            personalizedTips = listOf("매일 10분씩이라도 식단을 기록해 보세요."),
            nextStepsRecommendation = "다음 주에는 운동 강도를 한 단계 낮추고, 식단 기록에 집중해 보세요.",
            disclaimer = "더미 데이터로 생성된 분석입니다."
        )
        return flowOf(fakeAnalysis) // flowOf로 감싸서 Flow로 반환
    }
}