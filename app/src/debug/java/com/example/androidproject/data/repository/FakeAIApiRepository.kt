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
        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
        val todayDateString = dateFormat.format(Date())

        val validExerciseNames = listOf(
            "목 측면 굽힘", "어깨 올리기/내리기", "앉아서 몸통 좌우 회전", 
            "앉았다 일어서기 (의자 스쿼트)", "벽 짚고 팔 굽혀 펴기", 
            "제자리 무릎 들고 걷기", "종아리 스트레칭 (벽 밀기)"
        )

        val dummyScheduledWorkouts = (0..6).map { i ->
            val calendarWorkout = java.util.Calendar.getInstance()
            calendarWorkout.add(java.util.Calendar.DAY_OF_YEAR, i)
            val dateStr = dateFormat.format(calendarWorkout.time)

            val exerciseName = validExerciseNames[i % validExerciseNames.size]

            ScheduledWorkout(
                scheduledDate = dateStr,
                exercises = listOf(
                    ExerciseRecommendation(
                        name = exerciseName,
                        description = "AI 추천 운동: $exerciseName", bodyPart = "전신",
                        sets = 3, reps = 10, difficulty = "초급",
                        aiRecommendationReason = "매일 꾸준한 운동을 위해 구성했습니다.",
                        imageUrl = null
                    )
                )
            )
        }

        // 7일치 더미 데이터 생성
        val calendar = java.util.Calendar.getInstance()
        
        val dummyScheduledDiets = (0..6).map { i ->
            if (i > 0) calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val dateStr = dateFormat.format(calendar.time)
            
            ScheduledDiet(
                scheduledDate = dateStr,
                meals = listOf(
                    DietRecommendation(
                        mealType = "아침",
                        foodItems = listOf("오트밀 죽", "삶은 달걀 1개"),
                        ingredients = listOf("오트밀", "달걀", "우유"),
                        calories = 350.0, proteinGrams = 15.0, carbs = 45.0, fats = 10.0,
                        aiRecommendationReason = "아침은 가볍고 소화가 잘 되는 음식으로 구성했습니다."
                    ),
                    DietRecommendation(
                        mealType = "점심",
                        foodItems = listOf("닭가슴살 샐러드 (Debug)", "고구마 100g"),
                        ingredients = listOf("닭가슴살", "양상추", "토마토", "고구마"),
                        calories = 450.0, proteinGrams = 35.0, carbs = 50.0, fats = 12.0,
                        aiRecommendationReason = "점심은 활동 에너지를 위해 탄수화물과 단백질을 균형 있게 배치했습니다."
                    ),
                    DietRecommendation(
                        mealType = "저녁",
                        foodItems = listOf("연어 스테이크", "구운 야채"),
                        ingredients = listOf("연어", "아스파라거스", "버섯"),
                        calories = 400.0, proteinGrams = 30.0, carbs = 10.0, fats = 25.0,
                        aiRecommendationReason = "저녁은 소화 부담을 줄이고 단백질 위주로 구성했습니다."
                    )
                )
            )
        }

        val aiResult = AIRecommendationResult(
            scheduledWorkouts = dummyScheduledWorkouts,
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