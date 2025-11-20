package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.DietRecommendation
import com.example.androidproject.domain.model.ExerciseRecommendation
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.model.ScheduledDiet
import com.example.androidproject.domain.repository.AIApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FakeAIApiRepository @Inject constructor() : AIApiRepository {

    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {

        val satisfactionNote = if (params.pastSessions.any { it.userRating ?: 0 < 3 })
            "(Debug: 낮은 만족도 반영, 쉬운 운동으로 조정)"
        else
            "(Debug: 새 루틴)"

        // ViewModel의 날짜 형식과 '정확히' 일치하는 '오늘 날짜'의 데이터를 생성합니다.
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())

        val day1Workout = ScheduledWorkout(
            scheduledDate = todayDateString,
            exercises = listOf(
                ExerciseRecommendation(
                    name = "가벼운 손목 스트레칭 $satisfactionNote",
                    description = "손목을 부드럽게 돌려줍니다.", bodyPart = "손목",
                    sets = 2, reps = 15, difficulty = "초급",
                    aiRecommendationReason = "부상 부위(손목)의 가벼운 재활 시작",
                    imageUrl = null
                )
            )
        )

        val day2Workout = ScheduledWorkout(
            scheduledDate = "11월 17일 (월)", // (내일 데이터)
            exercises = listOf(
                ExerciseRecommendation(
                    name = "가벼운 스쿼트 (Debug)",
                    description = "하체 근력 유지", bodyPart = "하체",
                    sets = 3, reps = 10, difficulty = "초급",
                    aiRecommendationReason = "전신 근력 유지",
                    imageUrl = null
                )
            )
        )

        val dummyDiets = listOf(
            DietRecommendation(
                mealType = "점심",
                foodItems = listOf("닭가슴살 샐러드 (Debug)", "고구마 100g"),
                ingredients = listOf("닭가슴살", "양상추", "토마토", "고구마"),
                calories = 400.0, proteinGrams = 30.0, carbs = 40.0, fats = 15.0,
                aiRecommendationReason = "고단백 식단입니다."
            )
        )

        val dummyScheduledDiets = listOf(
            ScheduledDiet(
                scheduledDate = todayDateString,
                meals = dummyDiets
            )
        )

        val aiResult = AIRecommendationResult(
            scheduledWorkouts = listOf(day1Workout, day2Workout),
            scheduledDiets = dummyScheduledDiets,
            overallSummary = "AI가 생성한 2일치 재활 계획입니다. $satisfactionNote",
            disclaimer = "더미 데이터로 생성된 추천입니다."
        )

        emit(aiResult)
    }

    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> {
        val fakeAnalysis = AIAnalysisResult(
            summary = "AI 주간 분석 (Debug 모드): 지난 주 운동은 꾸준했습니다.",
            strengths = listOf("운동을 꾸준히 했습니다."),
            areasForImprovement = listOf("식단 기록이 부족합니다."),
            personalizedTips = listOf("식단을 기록해 보세요."),
            nextStepsRecommendation = "다음 주에도 꾸준히 진행해 보세요.",
            disclaimer = "더미 데이터로 생성된 분석입니다."
        )
        return flowOf(fakeAnalysis)
    }
}